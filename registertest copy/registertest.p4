#include <core.p4>
#include <v1model.p4>

// Description taken from NGSDN-TUTORIAL
// CPU_PORT specifies the P4 port number associated to controller packet-in and
// packet-out. All packets forwarded via this port will be delivered to the
// controller as P4Runtime PacketIn messages. Similarly, PacketOut messages from
// the controller will be seen by the P4 pipeline as coming from the CPU_PORT.
#define CPU_PORT 255

// Description taken from NGSDN-TUTORIAL
// CPU_CLONE_SESSION_ID specifies the mirroring session for packets to be cloned
// to the CPU port. Packets associated with this session ID will be cloned to
// the CPU_PORT as well as being transmitted via their egress port (set by the
// bridging/routing/acl table). For cloning to work, the P4Runtime controller
// needs first to insert a CloneSessionEntry that maps this session ID to the
// CPU_PORT.
#define CPU_CLONE_SESSION_ID 99

// Type aliases defined for convenience
typedef bit<9>   port_num_t;
typedef bit<48>  mac_addr_t;
typedef bit<32>  ipv4_addr_t;
typedef bit<16>  l4_port_t;

const bit<16> ETHERTYPE_IPV4 = 0x0800;



//------------------------------------------------------------------------------
// HEADER DEFINITIONS
//------------------------------------------------------------------------------

header ethernet_t {
    mac_addr_t  dst_addr;
    mac_addr_t  src_addr;
    bit<16>     ether_type;
}


header ipv4_t {
    bit<4>   version;
    bit<4>   ihl;
    bit<6>   dscp;
    bit<2>   ecn;
    bit<16>  total_len;
    bit<16>  identification;
    bit<3>   flags;
    bit<13>  frag_offset;
    bit<8>   ttl;
    bit<8>   protocol;
    bit<16>  hdr_checksum;
    bit<32>  src_addr;
    bit<32>  dst_addr;
}



header udp_t {
    bit<16> srcPort;
    bit<16> dstPort;
    bit<16> length;
    bit<16> checksum;
}

header gtp_t {
    bit<8>  flags;
    bit<8>  msgType;
    bit<16> length;
    bit<32> teid;
}

header gtp_optional_t{
    bit<8> sequence_number_1;
    bit<8> sequence_number_2;
    bit<8> N_PDU;
    bit<8> next_extension_header_type;
}

header extension_header_t{
    bit<8> length;
    bit<8> pdu_session;
    bit<8> QFI;
    bit<8> extension_header;
}

// Description taken from NGSDN-TUTORIAL
// Packet-in header. Prepended to packets sent to the CPU_PORT and used by the
// P4Runtime server (Stratum) to populate the PacketIn message metadata fields.
// Here we use it to carry the original ingress port where the packet was
// received.
@controller_header("packet_in")
header cpu_in_header_t {
    port_num_t  ingress_port;
    bit<7>      _pad;
}

// Description taken from NGSDN-TUTORIAL
// Packet-out header. Prepended to packets received from the CPU_PORT. Fields of
// this header are populated by the P4Runtime server based on the P4Runtime
// PacketOut metadata fields. Here we use it to inform the P4 pipeline on which
// port this packet-out should be transmitted.
@controller_header("packet_out")
header cpu_out_header_t {
    port_num_t  egress_port;
    bit<7>      _pad;
}

struct parsed_headers_t {
    cpu_out_header_t cpu_out;
    cpu_in_header_t cpu_in;
    ethernet_t ethernet;
    ipv4_t ipv4;
    udp_t udp;
    gtp_t gtp;
    gtp_optional_t gtp_optional;
    extension_header_t extension_header;
    ipv4_t inner_ipv4;
    udp_t inner_udp;
}


struct metadata {
    @field_list(1)
    port_num_t ingress_port;
    bit<32> flowID;
}


//------------------------------------------------------------------------------
// INGRESS PIPELINE
//------------------------------------------------------------------------------

parser ParserImpl (packet_in packet,
                   out parsed_headers_t hdr,
                   inout metadata meta,
                   inout standard_metadata_t standard_metadata)
{
    state start {
        meta.ingress_port = standard_metadata.ingress_port;
        transition select(standard_metadata.ingress_port) {
            CPU_PORT: parse_packet_out;
            default: parse_ethernet;
        }
    }

    state parse_packet_out {
        packet.extract(hdr.cpu_out);
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.ether_type){
            ETHERTYPE_IPV4: parse_ipv4;
            default: accept;
        }
    }


    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition select(hdr.ipv4.protocol) {
            17: parse_udp;
            default: accept;
        }
    }

    state parse_udp {
        packet.extract(hdr.udp);
        transition select(hdr.udp.dstPort) {
            2152: parse_gtp;
            default: accept;
        }
    }

    state parse_gtp {
        packet.extract(hdr.gtp);
        transition select(hdr.gtp.flags) {
            0x34: parse_gtp_optional;
            default: accept;
        }
    }

    state parse_gtp_optional {
        packet.extract(hdr.gtp_optional);
        transition select(hdr.gtp_optional.next_extension_header_type) {
            0x85: parse_extension_header;
            default: accept;
        }
    }

    state parse_extension_header {
        packet.extract(hdr.extension_header);
        transition select(hdr.extension_header.QFI){
            1: parse_inner_ipv4;
            default: accept;
        }
    }

    state parse_inner_ipv4 {
        packet.extract(hdr.inner_ipv4);
        transition select(hdr.inner_ipv4.protocol) {
            17: parse_inner_udp;
            default: accept;
        }
    }

    state parse_inner_udp {
        packet.extract(hdr.inner_udp);
        transition accept;
    }




}


control VerifyChecksumImpl(inout parsed_headers_t hdr,
                           inout metadata meta)
{
    // Description taken from NGSDN-TUTORIAL
    // Not used here. We assume all packets have valid checksum, if not, we let
    // the end hosts detect errors.
    apply { /* EMPTY */ }
}


control IngressPipeImpl (inout parsed_headers_t    hdr,
                         inout metadata    meta,
                         inout standard_metadata_t standard_metadata) {

    
    bool dropped = false;
    bool pass = true; 
    bool acl = true;
    bool gtp = false;

    action set_gtp(){
        pass = false;
        gtp = true;
    }

    table gtp_check{
        key = {
            hdr.udp.dstPort: exact;
            hdr.udp.srcPort: exact;
        }
        actions = {
            set_gtp;
        }
        const entries = {
            {2152, 2152}: set_gtp();
        }
        @name("gtp_check_counter")
        counters = direct_counter(CounterType.packets_and_bytes);
    }
    



    // Drop action shared by many tables.
    action drop() {
        mark_to_drop(standard_metadata);
        dropped = true;
        acl = false;
    }

    action send_to_port(port_num_t port_num) {
        standard_metadata.egress_spec = port_num;
        acl = false;
    }




    table ipv4_check{
        key = {
            hdr.ipv4.dst_addr: exact;
        }
        actions = {
            drop;
            send_to_port;
        }
        default_action = send_to_port(5);
        const entries = {
            {0xC0A8E902}: send_to_port(0);
            {0xC0A8E903}: send_to_port(1);
            {0xC0A8E904}: send_to_port(2);
            {0xC0A8E905}: send_to_port(3);
            {0xC0A8E906}: send_to_port(4);
        }
        @name("ipv4_check_counter")
        counters= direct_counter(CounterType.packets_and_bytes);
    }
   

    
    action track_tunnel(){
        acl = false;
	    pass = true;
    }

    table gtp_tunnel{
        key ={
            hdr.inner_ipv4.src_addr: exact;
            hdr.inner_ipv4.dst_addr: exact;
            hdr.inner_ipv4.protocol: exact;
        }
        actions = {
            drop; 
            track_tunnel;   
        }
        @name("gtp_tunnel_counter")
        counters = direct_counter(CounterType.packets_and_bytes);
    }
    

    action send_to_cpu() {
        standard_metadata.egress_spec = CPU_PORT;
    }

    action clone_to_cpu() {
        clone_preserving_field_list(CloneType.I2E, CPU_CLONE_SESSION_ID,1);
    }

    table acl_table {
        key = {
            standard_metadata.ingress_port: ternary;
            hdr.ethernet.dst_addr:          ternary;
            hdr.ethernet.src_addr:          ternary;
            hdr.ethernet.ether_type:        ternary;
        }
        
        actions = {
            send_to_cpu;
            clone_to_cpu;
            drop;
        }
        default_action = send_to_cpu();
        @name("acl_table_counter")
        counters = direct_counter(CounterType.packets_and_bytes);
    }
    apply {

        if(hdr.cpu_out.isValid()){
            standard_metadata.egress_spec = hdr.cpu_out.egress_port;
            hdr.cpu_out.setInvalid();
            exit;
        }

        if(hdr.ethernet.isValid() && hdr.ipv4.isValid()){
            gtp_check.apply();
            gtp_tunnel.apply();
            if(dropped == false && pass == true){
                ipv4_check.apply();
            }



        }
        if(acl == true){
            acl_table.apply();
        }
        
    }
}


control EgressPipeImpl (inout parsed_headers_t hdr,
                        inout metadata meta,
                        inout standard_metadata_t standard_metadata) {
    apply {

        if (standard_metadata.egress_port == CPU_PORT) {
            // *** TODO EXERCISE 4
            // Implement logic such that if the packet is to be forwarded to the
            // CPU port, e.g., if in ingress we matched on the ACL table with
            // action send/clone_to_cpu...
            // 1. Set cpu_in header as valid
            // 2. Set the cpu_in.ingress_port field to the original packet's
            //    ingress port (standard_metadata.ingress_port).

            hdr.cpu_in.setValid();
            hdr.cpu_in.ingress_port = meta.ingress_port;
            exit;
        }

    }
}


control ComputeChecksumImpl(inout parsed_headers_t hdr,
                            inout metadata meta)
{
    apply {

    }
}


control DeparserImpl(packet_out packet, in parsed_headers_t hdr) {
    apply {
        packet.emit(hdr.cpu_in);
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.udp);
        packet.emit(hdr.gtp);
        packet.emit(hdr.gtp_optional);
        packet.emit(hdr.extension_header);
        packet.emit(hdr.inner_ipv4);
        packet.emit(hdr.inner_udp);
    }
}


V1Switch(
    ParserImpl(),
    VerifyChecksumImpl(),
    IngressPipeImpl(),
    EgressPipeImpl(),
    ComputeChecksumImpl(),
    DeparserImpl()
) main;
