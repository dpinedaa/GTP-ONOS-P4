from scapy.layers.l2 import Ether
from scapy.layers.inet import IP, UDP
from scapy.contrib.gtp import GTP_U_Header, GTPPDUSessionContainer
from scapy.sendrecv import sendp

def create_tunnel_packet(teid):
    outeth = Ether(src='52:54:00:c4:5c:17', dst='52:54:00:5a:bd:1b', type=0x800)
    outip = IP(src='192.168.233.2', dst='192.168.233.3', proto=17)
    outudp = UDP(sport=2152, dport=2152)
    gtp = GTP_U_Header(E=1, teid=teid, gtp_type=0xff, next_ex=0x85)
    gscon = GTPPDUSessionContainer(QFI=1)
    inip = IP(dst='10.45.0.3', src='10.45.0.3', proto=17)
    inudp = UDP(sport=2000, dport=2000)
    data = 'Hello world'
    packet = outeth / outip / outudp / gtp / gscon / inip / inudp / data
    return packet

# Send multiple GTP-U packets
num_packets = 20  # You can change this to the desired number of packets
for teid in range(1, num_packets + 1):
    packet = create_tunnel_packet(teid)
    sendp(packet, iface='veth1')
