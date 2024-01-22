<template>
    <div class="layout">
        <div class="header">
            <h1>Flows</h1>
        </div>
    </div>

    <div>
        <button class="nav-btn" @click="resetFlows"><i class="fas fa-sync-alt"></i></button>
    </div>

</template>

<script>
import axios from 'axios';

export default{
    methods:{
        async resetFlows(){
            await axios.delete('http://10.102.211.11:3000/flows')
            .then(res => {
                console.log(res);
            })
            .catch(err => {
                console.log(err);
            })

            // await axios.delete('http://10.102.211.153:4000/unidirectionalflows')
            // .then(res => {
            //     console.log(res);
            // })
            // .catch(err => {
            //     console.log(err);
            // })
            


        }
    }
}
</script>


<style>

/*Nav bar button style*/
.nav-btn{
border-radius: 5px;
height: 100%;
width: 40px;
background-color: black;
color: white;
border: none;
font-size: 18px;
text-align: center;
font-weight: bold;
padding: 10px;
margin: 5px;
/* border: 1px solid darkgray; */

}

.nav-btn:hover{
background-color: #e6e6e6;

color: black;
}


</style>

