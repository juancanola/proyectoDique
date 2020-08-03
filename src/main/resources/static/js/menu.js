var chart;
var intervalo;


$(document).ready(function() {



	
	
	$("#inputModo").click(function(e) {

		var chequeadoApp = $('#inputModo').is(":checked") 
		console.log(chequeadoApp);

		 $.ajax({
			    type: "POST",
	            url: "/packets/cambiarmodo",
	            data: {    tipomodo : chequeadoApp},
	            success: function(data) {
	            	//console.log(data);
	            },
	            error: function() {}
	        }); 


	});
	
	obtenerDatosGrafica();
	
		var refreshTable = $('#packetList').DataTable({
			pageLength : 4,
			ajax : {
				url : '/packets/findAllPackets',
				dataSrc : ''
			},
			columns : [ 
			{
				title : 'Id',
				data : 'id'
			}, {
				title : 'IP Origen',
				data : 'srcip'
			}, {
				title : 'Puerto Origen',
				data : 'srcport'
			}, {
				title : 'IP Destino',
				data : 'dstip'
			}, {
				title : 'Puerto Destino',
				data : 'dstport'
			}, {
				title : 'Protocolo',
				data : 'protocol'
			}, {
				title : 'Tipo',
				data : 'type'
			}
		 ],
		 "columnDefs": [ {
			    "targets": 6,
			    "render": function ( data, type, row, meta ) {
			    	if(data==true){
			    		
			    
			    		 return '<b style="color:red;">Maligno</b>';
			    		
			    		}
			    	else{
			    		 return '<b style="color:green;">Normal</b>';
			    	}
			    	
			     
			    }
			  } ]
		});
		
		function obtenerDatosGrafica(){
		 $.ajax({

				url: '/packets/obtenerdatosgrafica',
				success: function(result){
					var formatteddata = [];
					for(var key in result){

			
						var singleObject = {
								name: '',
								color:'',
								data: []
		
							}
						singleObject.name = key.toUpperCase();
						for(var i = 0; i < result[key].length; i++){
							if(key=='Maligno'){
								singleObject.color='#FF0000'
							}
							else if(i==1){
								singleObject.color='#00FF00'
							}

						
							singleObject.data.push([parseInt(result[key][i].time),parseInt(result[key][i].count)]);
			
							
						}
						formatteddata.push(singleObject);
					}
					//console.log(formatteddata);
					drawMultipleLineChart(formatteddata);

				}
			});
		}

			function drawMultipleLineChart(formatteddata){
				 chart = Highcharts.chart('containerGrafica', {
					title:{
					      text: null
					      },
					   xAxis: {
					        type: 'datetime'
					    },
				    yAxis: {
				        title: {
				            text: 'Cantidad de paquetes'
				        }
				    },
				    legend: {
				        layout: 'vertical',
				        align: 'right',
				        verticalAlign: 'middle'
				    },

				    plotOptions: {
				    	 pointStart: 2000,
				        series: {
				        	animation: false,
				            label: {
				                connectorAllowed: false
				            }
				        }
				    },

				    series: formatteddata,
				    responsive: {
				        rules: [{
				            condition: {
				                maxWidth: 500
				            },
				            chartOptions: {
				                legend: {
				                    layout: 'horizontal',
				                    align: 'center',
				                    verticalAlign: 'bottom'
				                }
				            }
				        }]
				    }

				});
			}
	
			
			

	 
	 



	 
	 

         
         //BUTON
   $("#btnCapturar").click(function(e) {

	   var valor = document.getElementById('btnCapturar').innerText ;

	   if(valor=='Capturar')
		   {
			intervalo = setInterval(function tiempo() {
				 $('#packetList').DataTable().ajax.reload();
				 //insertar();
				 console.log('work1');
				 obtenerDatosGrafica();
				
			}, 100);
			
//			intervalo2 = setInterval(function tiempo2() {
//
//				 insertarPaquetes();
//				
//			}, 1);
			
		   document.getElementById('btnCapturar').innerText = 'Detener';
		    e.preventDefault();
	        $.ajax({
	       	 
	            url : '/packets/capturarPaquetes',
	            global: false,
	            type: 'GET',
	            data: {},
	            async: true, //blocks window close
	            success: function() {},
	            error: function() {}
	        }); 
		   
		   }
	   else{
		   clearInterval(intervalo);
		   intervalo=0;
		   console.log('detener');
		   document.getElementById('btnCapturar').innerText = 'Capturar';
		   
		    e.preventDefault();
	        $.ajax({
	       	 
	            url : '/packets/detenerPaquetes',
	            global: false,
	            type: 'GET',
	            data: {},
	            async: true, //blocks window close
	            success: function() {},
	            error: function() {}
	        }); 
	   }
	  
	   
	
	});   
         
   
  $("#btnReiniciar").click(function(e) {
		
	
	        $.ajax({
	       	 
	            url : '/packets/reiniciar',
	            global: false,
	            type: 'GET',
	            data: {},
	            async: true, //blocks window close
	            success: function() {
	        	    e.preventDefault();
	   	      	 $('#packetList').DataTable().ajax.reload();
	       		 obtenerDatosGrafica();
	            },
	            error: function() {
	        	    e.preventDefault();
	   	      	 $('#packetList').DataTable().ajax.reload();
	       		 obtenerDatosGrafica();
	            	
	            }
	        }); 
		   
		   

	});   

   
  
	 
	});
	
	function insertarPaquetes(){
		
		   
		  $.ajax({
	            type : "GET",
	            url : "/packets/insertarPaquetes",
	            success : function(result) {
	                console.log("SUCCESS: ");
	      
	            },
	            error: function(e){
	                console.log("ERROR: ", e);
	       
	            },
	            done : function(e) {
	                console.log("DONE");
	            }
	    });
	           };
	           
	           
	           
	           
	           
	           
	           
	           
	           
	           //AL CARGAR LA PAGINA

	           window.onload = function () {
	             
	           		 $('#packetList').DataTable().ajax.reload();

	           		 obtenerDatosGrafica();
	      
	           	
	        	function obtenerDatosGrafica(){
	       		 $.ajax({

	       				url: '/packets/obtenerdatosgrafica',
	       				success: function(result){
	       					var formatteddata = [];
	       					for(var key in result){

	       			
	       						var singleObject = {
	       								name: '',
	       								color:'',
	       								data: []
	       		
	       							}
	       						singleObject.name = key.toUpperCase();
	       						for(var i = 0; i < result[key].length; i++){
	       							if(key=='Maligno'){
	       								singleObject.color='#FF0000'
	       							}
	       							else if(i==1){
	       								singleObject.color='#00FF00'
	       							}

	       						
	       							singleObject.data.push([parseInt(result[key][i].time),parseInt(result[key][i].count)]);
	       			
	       							
	       						}
	       						formatteddata.push(singleObject);
	       					}
	       					//console.log(formatteddata);
	       					drawMultipleLineChart(formatteddata);

	       				}
	       			});
	       		}

	       			function drawMultipleLineChart(formatteddata){
	       				 chart = Highcharts.chart('containerGrafica', {
	       					title:{
	       					      text: null
	       					      },
	       					   xAxis: {
	       					        type: 'datetime'
	       					    },
	       				    yAxis: {
	       				        title: {
	       				            text: 'Cantidad de paquetes'
	       				        }
	       				    },
	       				    legend: {
	       				        layout: 'vertical',
	       				        align: 'right',
	       				        verticalAlign: 'middle'
	       				    },

	       				    plotOptions: {
	       				    	 pointStart: 2000,
	       				        series: {
	       				        	animation: false,
	       				            label: {
	       				                connectorAllowed: false
	       				            }
	       				        }
	       				    },

	       				    series: formatteddata,
	       				    responsive: {
	       				        rules: [{
	       				            condition: {
	       				                maxWidth: 500
	       				            },
	       				            chartOptions: {
	       				                legend: {
	       				                    layout: 'horizontal',
	       				                    align: 'center',
	       				                    verticalAlign: 'bottom'
	       				                }
	       				            }
	       				        }]
	       				    }

	       				});
	       			}
	           }