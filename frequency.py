import argparse
from scapy.layers.l2 import Ether
from scapy.layers.inet import IP  # Updated import
from scapy.layers.inet import UDP
from scapy.contrib.gtp import GTP_U_Header, GTPPDUSessionContainer
from scapy.sendrecv import sendp
import time


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

def send_packets(tunnel_number, packet_count, delay, iface):
    packet = create_tunnel_packet(tunnel_number)
    for i in range(packet_count):
        sendp(packet, iface=iface, inter = delay)

def main():
    parser = argparse.ArgumentParser(description="Send GTP-U packets with a specified tunnel number.")
    parser.add_argument("tunnel_number", type=int, help="The tunnel number to use")

    args = parser.parse_args()
    
    packet_count = 1000  # Number of packets to send
    delay = 0.000000000001  # Delay between packets in seconds
    iface = 'veth1'  # Change the network interface as needed

    send_packets(args.tunnel_number, packet_count, delay, iface)

if __name__ == "__main__":
    main()
