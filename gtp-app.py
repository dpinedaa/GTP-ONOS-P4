import struct

# Your provided hexadecimal string
hex_string = "525400edcec4525400d5aea70800450000804c74400040119a9dc0a8e905c0a8e90408680868006c6e0634ff005c0000f4f3000000850110010045000054ed214000400133480a2d0003080808080800375c00250001c182a065000000009dc2020000000000101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031323334353637"

# for i in range(0, len(hex_string), 2):
    # print("Position:", i , hex_string[i:i+2])

#hex_string[32:36] is the total length
#hex_string[140:148] is the source IP
#hex_string[148:156] is the destination IP
#hex_string[134:136] is the protocol
    
total_length_dec = int(hex_string[32:36], 16)
print("Total Length:", total_length_dec)

src_ip_dec = ".".join(map(str, [int(hex_string[140:148][i:i+2], 16) for i in range(0, len(hex_string[140:148]), 2)]))
print("Source IP:", src_ip_dec)

dst_ip_dec = ".".join(map(str, [int(hex_string[148:156][i:i+2], 16) for i in range(0, len(hex_string[148:156]), 2)]))
print("Destination IP:", dst_ip_dec)

protocol_dec = int(hex_string[134:136], 16)
print("IPv4 Protocol:", protocol_dec)








