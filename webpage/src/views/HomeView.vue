<template>
  <div class="layout">
    <div class="content">
      <div>
        <h1>Blocked UE IP addresses</h1>
        <table class="ip-table">
          <thead>
            <tr>
              <th>Blocked UE IP address</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="blockedIP in blockedIPs" :key="blockedIP._id">
              <td>{{ blockedIP.srcIP }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <h1>Current Flows</h1>
      <table class="flow-table">
        <thead>
          <tr>
            <th colspan="2"></th>
            <th colspan="3">Forward</th>
            <th colspan="3">Backward</th>
            <th colspan="6">Unidirectional Flow</th>
          </tr>
          <tr>
            <th>IP Address</th>
            <th>Protocol</th>
            <th>Duration</th>
            <th>Packet count</th>
            <th>Byte count</th>
            <th>Duration</th>
            <th>Packet count</th>
            <th>Byte count</th>
            <th>Flow Bytes/s</th>
            <th>Backward Packet/s</th>
            <th>LR prediction</th>
            <th>RF prediction</th>
            <th>NB prediction</th>
            <th>KNN prediction</th>
          </tr>
        </thead>
        <tbody>
          <!-- Iterate over flows -->
          <template v-for="(flow, index) in flows" :key="flow._id">
            <!-- Check if a new group begins (based on srcIP) -->
            <template v-if="index === 0 || flow.srcIP !== flows[index - 1].srcIP">
              <tr>
                <td colspan="14" class="group-header">{{ flow.srcIP }}</td>
              </tr>
            </template>
            <!-- Flow row -->
            <tr>
              <td>{{ flow.srcIP }} <i class="fas fa-arrows-alt-h"></i> {{ flow.dstIP }}</td>
              <td>
                <!-- if protocol is 1 -->
                <template v-if="flow.fwdFlow.protocol == 1">ICMP</template>
                <!-- if protocol is 6 -->
                <template v-else-if="flow.fwdFlow.protocol == 6">TCP</template>
                <!-- if protocol is 17 -->
                <template v-else-if="flow.fwdFlow.protocol == 17">UDP</template>
              </td>
              <td>{{ flow.forwardDuration }}</td>
              <td>{{ flow.forwardPacketCount }}</td>
              <td>{{ flow.forwardByteCount }}</td>
              <td>{{ flow.backwardDuration }}</td>
              <td>{{ flow.backwardPacketCount }}</td>
              <td>{{ flow.backwardByteCount }}</td>
              <td>{{ flow.flowBytesPerSecond }}</td>
              <td>{{ flow.backwardPacketsPerSecond }}</td>



              <td>
                <!-- Check the vaues for lr  -->
                <!-- If it's less than 0.5 put the style font color as green and bold  -->
                <div v-if="flow.lrPrediction < 0.5" style="color: green; font-weight: bold">
                  {{ flow.lrPrediction }}
                </div>
                
                <!-- Value is between 0.5 and 0.8 font color is orange -->
                <div v-else-if="flow.lrPrediction >= 0.5 && flow.lrPrediction < 0.8" style="color: orange; font-weight: bold;">
                  {{ flow.lrPrediction }}
                </div>

                <!-- Value is between 0.8 and 1 in red-->
                <div v-else style="color: red; font-weight: bold;">
                  {{ flow.lrPrediction }}
                </div>
              </td>


              <td>
                <div v-if="flow.rfPrediction < 0.5" style="color: green; font-weight: bold">
                  {{ flow.rfPrediction }}
                </div>
                <div v-else-if="flow.rfPrediction >= 0.5 && flow.rfPrediction < 0.8" style="color: orange; font-weight: bold;">
                  {{ flow.rfPrediction }}
                </div>
                <div v-else style="color: red; font-weight: bold;">
                  {{ flow.rfPrediction }}
                </div>
              </td>
              <td>
                <div v-if="flow.nbPrediction < 0.5" style="color: green; font-weight: bold">
                  {{ flow.nbPrediction }}
                </div>
                <div v-else-if="flow.nbPrediction >= 0.5 && flow.nbPrediction < 0.8" style="color: orange; font-weight: bold;">
                  {{ flow.nbPrediction }}
                </div>
                <div v-else style="color: red; font-weight: bold;">
                  {{ flow.nbPrediction }}
                </div>
              </td>

              <td>
                <div v-if="flow.knnPrediction < 0.5" style="color: green; font-weight: bold">
                  {{ flow.knnPrediction }}
                </div>
                <div v-else-if="flow.knnPrediction >= 0.5 && flow.knnPrediction < 0.8" style="color: orange; font-weight: bold;">
                  {{ flow.knnPrediction }}
                </div>
                <div v-else style="color: red; font-weight: bold;">
                  {{ flow.knnPrediction }}
                </div>

              </td>


              
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      flows: [],
      blockedIPs: [],
    };
  },
  mounted() {
    // this.deleteAllFlows();
    this.getFlows();
    this.getBlockedIPs();
    setInterval(this.refreshPage, 5000); // Reload every 5 seconds
    
  },
  methods: {
    getFlows() {
      const apiUrl = 'http://10.102.211.11:3000/flows';
      axios.get(apiUrl)
        .then(response => {
          console.log(response);
          this.flows = response.data.sort((a, b) => {
            const ipA = a.srcIP.split('.').map(Number);
            const ipB = b.srcIP.split('.').map(Number);
  
            for (let i = 0; i < 4; i++) {
              if (ipA[i] !== ipB[i]) {
                return ipA[i] - ipB[i];
              }
            }
            return 0;
          });
          console.log(this.flows);
        })
        .catch(error => {
          console.log(error);
        });
    },

    getBlockedIPs() {
      const apiUrl = 'http://10.102.211.11:3001/ips';
      axios.get(apiUrl)
        .then(response => {
          console.log(response);
          this.blockedIPs = response.data;
          console.log(response.data);
        })
        .catch(error => {
          console.log(error);
        });
    },
    refreshPage() {
      location.reload();
    },

    deleteAllFlows() {
      const apiUrl = 'http://10.102.211.11:3000/flows';
      axios.delete(apiUrl)
        .then(response => {
          console.log(response);
          this.flows = [];
        })
        .catch(error => {
          console.log(error);
        });

    }
  },
};
</script>

<style>
.layout {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  margin-right: 15%;
  margin-left: 15%;
}

.content {
  flex: 1;
  width: 100%;
  margin-top: 50px;
  color: black;
  box-sizing: border-box;
  overflow-x: auto;
}

.flow-table {
  border-collapse: collapse;
  width: 100%;
  border: 1px solid black;
}

.flow-table th {
  border: 1px solid black;
  text-align: center;
  padding: 8px;
  background-color: rgb(78, 78, 78);
  color: white;
  font-size: small;
  width: 5%;
}

.flow-table th[colspan="6"] {
  border: 2px solid black;
}

.flow-table td {
  border: 1px solid black;
  text-align: center;
  padding: 8px;
  background-color: white;
  color: black;
  font-size: small;
  width: 5%;
}

@media screen and (min-width: 600px) {
  .content {
    width: 90%;
  }
}

.ip-table {
  width: 30%;
  border: 1px solid black;
  align-items: center;
  margin-left: 35%;
}

.ip-table th {
  border: 1px solid black;
  text-align: center;
  padding: 8px;
  background-color: rgb(172, 42, 42);
  color: white;
  font-size: medium;
  width: 5%;
}

.ip-table th[colspan="6"] {
  border: 2px solid black;
}

.ip-table td {
  border: 1px solid black;
  text-align: center;
  padding: 8px;
  background-color: white;
  color: black;
  font-size: small;
  width: 5%;
}

.group-header {
  font-weight: bold;
  border-bottom: 2px solid black;
}
</style>