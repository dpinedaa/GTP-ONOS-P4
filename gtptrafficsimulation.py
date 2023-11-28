from scapy.layers.l2 import Ether
from scapy.layers.inet import IP, UDP
from scapy.contrib.gtp import GTP_U_Header, GTPPDUSessionContainer
from scapy.sendrecv import sendp

def create_tunnel_packet(inner_src_ip, inner_dst_ip, inner_src_port, inner_dst_port, teid):
    outeth = Ether(src='52:54:00:c4:5c:17', dst='52:54:00:5a:bd:1b', type=0x800)
    outip = IP(src='192.168.233.2', dst='192.168.233.3', proto=17)
    outudp = UDP(sport=2152, dport=2152)
    gtp = GTP_U_Header(E=1, teid=teid, gtp_type=0xff, next_ex=0x85)
    gscon = GTPPDUSessionContainer(QFI=1)
    inip = IP(dst=inner_dst_ip, src=inner_src_ip, proto=17)
    inudp = UDP(sport=inner_src_port, dport=inner_dst_port)
    data = 'Hello world'
    packet = outeth / outip / outudp / gtp / gscon / inip / inudp / data
    return packet

# Send multiple GTP-U packets
num_packets = 100  # You can change this to the desired number of packets


#Create an array with different IP addresses

src_ip_address = ['10.45.0.3','10.45.0.4','10.45.0.5','10.45.0.6','10.45.0.7','10.45.0.8','10.45.0.9','10.45.0.10']
dst_ip_address = ['8.8.8.8','1.1.1.1','1.0.0.1','8.8.4.4','208.67.222.222','208.67.220.22']

#Create an array with different ports
src_ports = [2000,2001,2002,2003,2004,2005,2006,2007,2008,2009]
dst_ports = [2000,2001,2002,2003,2004,2005,2006,2007,2008,2009]

#Create a function to generate a random gtp tunnel ID
import random
def generate_teid():
    teid = random.randint(1, 4294967295)
    return teid

#Number of packets to create flows combinations
num_flows = 100

#What are the possible combinations?
#1. Source IP address
#2. Destination IP address
#3. Source port
#4. Destination port
#5. GTP tunnel ID

#Create a function to generate a random flow

def pick_src_ip():
    src_ip = random.choice(src_ip_address)
    return src_ip

def pick_dst_ip():
    dst_ip = random.choice(dst_ip_address)
    return dst_ip

def pick_src_port():
    src_port = random.choice(src_ports)
    return src_port

def pick_dst_port():
    dst_port = random.choice(dst_ports)
    return dst_port

def pick_teid():
    teid = random.randint(1, 4294967295)
    return teid


for i in range(num_flows):
    inner_src_ip = pick_src_ip()
    inner_dst_ip = pick_dst_ip()
    inner_src_port = pick_src_port()
    inner_dst_port = pick_dst_port()
    inner_teid = pick_teid()
    packet = create_tunnel_packet(inner_src_ip, inner_dst_ip, inner_src_port, inner_dst_port, inner_teid)
    sendp(packet, iface='veth1')


    
