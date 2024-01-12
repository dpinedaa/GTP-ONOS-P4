from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_3
import binascii
import socket

class L2Switch(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def init(self, *args, **kwargs):
        super(L2Switch, self).init(*args, **kwargs)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def packet_in_handler(self, ev):
        print("Hi Diana!!!!")
        msg = ev.msg
        dp = msg.datapath
        ofp = dp.ofproto
        ofp_parser = dp.ofproto_parser
	
        # Print the received packet in hexadecimal format
        print("Message data:", msg.data)


        hex_data = binascii.hexlify(msg.data).decode('utf-8')
        print("Received Packet (hex):", hex_data)

        
        total_length_hex = hex_data[8:12]
        total_length_dec = int(total_length_hex, 16)
        print("Total Length:", total_length_dec)

        #total_length_hex = binascii.hexlify(msg.data[2:4]).decode('utf-8')
        # total_length_decimal = int(total_length_hex, 16)
        # Convert the total length to a string
        # total_length_string = str(total_length_decimal)

        # print("Total Length as string:", total_length_string)



        hex_data = "525400edcec4525400d5aea7080045000080006640004011e6abc0a8e905c0a8e90408680868006cd5da34ff005c00008d1f00000085011001004500005477c140004001a8a80a2d00030808080808004ba400200001cf24a0650000000070dd0d0000000000101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031323334353637"
        total_length_hex = hex_data[8:12]
        # Reverse the byte order to account for big endian format
        total_length_hex = total_length_hex[2:] + total_length_hex[:2]
        total_length_dec = int(total_length_hex, 16)
        print("Total Length:", total_length_dec)

        if msg.data[70:74] == b'\n-\x00\x03' or msg.data[74:78] == b'\n-\x00\x03':
            match = ofp_parser.OFPMatch(in_port=msg.match['in_port'])
            inst = [ofp_parser.OFPInstructionActions(ofp.OFPIT_CLEAR_ACTIONS, [])]
            mod = ofp_parser.OFPFlowMod(datapath=dp, buffer_id=ofp.OFP_NO_BUFFER, priority=1, match=match, instructions=inst)
            dp.send_msg(mod)
        else:
            actions = [ofp_parser.OFPActionOutput(ofp.OFPP_FLOOD)]
            out = ofp_parser.OFPPacketOut(datapath=dp, buffer_id=msg.buffer_id, in_port=msg.match['in_port'], actions=actions, data=msg.data)
            dp.send_msg(out)
