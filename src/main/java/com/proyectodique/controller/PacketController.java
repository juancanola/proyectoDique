package com.proyectodique.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyectodique.capturador.Capturadora;
import com.proyectodique.entity.Packet;
import com.proyectodique.entity.PacketGrafica;
import com.proyectodique.repository.PacketRepository;
import com.proyectodique.util.UtilidadesJuan;


@Controller
@RequestMapping("/packets")
public class PacketController {

	static Capturadora capturadora;
	
	@Autowired
	private PacketRepository packetRepository;	
	
	@RequestMapping(value = "findAllPackets", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Packet>> findAll() {
		try {
			return new ResponseEntity<List<Packet>>(packetRepository.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Packet>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping("/obtenerdatosgrafica")
	public ResponseEntity<?> getDataForMultipleLine() {
		List<Object[]> dataListPacket = packetRepository.findByTimeType();
		Map<String, List<PacketGrafica>> mappedData = new HashMap<>();
		
		for(Object[] obj : dataListPacket){
			PacketGrafica packetGrafica = new PacketGrafica();
		     
			if ((boolean) obj[0])
				packetGrafica.setType("Maligno");
			else
				packetGrafica.setType("Normal");
			    packetGrafica.setCount(String.valueOf(obj[1]));
			    packetGrafica.setTime(UtilidadesJuan.convertirFecha(obj[2]));
		   
		     
				if(mappedData.containsKey(packetGrafica.getType())) {
				mappedData.get(packetGrafica.getType()).add(packetGrafica);
			}else {
				List<PacketGrafica> tempList = new ArrayList<PacketGrafica>();
				tempList.add(packetGrafica);
				mappedData.put(packetGrafica.getType(), tempList);
			}

		     }
		
//		Map<String, List<Object[]>> mappedData = new HashMap<>();
//		for(GraphicPacket data : dataListPacket) {
//			
//			if(mappedData.containsKey(data.getType())) {
//				mappedData.get(data.getType()).add(data);
//			}else {
//				List<GraphicPacket> tempList = new ArrayList<GraphicPacket>();
//				tempList.add(data);
//				mappedData.put(data.getType(), tempList);
//			}
//			
//		}
		return new ResponseEntity<>(mappedData, HttpStatus.OK);
	}
	
	@GetMapping("/menu")
	public String menu() {
		return "menu";
	}
	
	@ResponseBody
	@GetMapping("/capturarPaquetes")
	public void capturarPaquetes() {
		 capturadora = new Capturadora();
		try {
			capturadora.capturar(packetRepository);
		} catch (IOException | PcapNativeException | NotOpenException e) {
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	
	}
	
	@ResponseBody
	@GetMapping("/detenerPaquetes")
	public void detenerPaquetes(){
		capturadora.detener();
		
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/reiniciar", method = RequestMethod.GET)
	//@GetMapping("/reiniciar")
	public void reiniciar() {
		try {
			packetRepository.deleteAll();
		} catch (Exception e) {
		
		}
		
	}
	
	
//	@ResponseBody
//	@GetMapping("/insertarPaquetes")
//	public String insertarPaquetes() {
//
//		Long idPacket= capturadora.insertarPaquete(packetRepository,packetReader);
//		
//		
//		return idPacket+" insertado";
//	
//	}
	
	
	@ResponseBody
    @RequestMapping(method = RequestMethod.POST,
    path = "/cambiarmodo")
    public String cambiarModo(@RequestParam("tipomodo") String tipoModo)
	    {
		boolean modo = Boolean.parseBoolean(tipoModo);
		UtilidadesJuan.modoApp = modo;
		return tipoModo;
	    }
	
	

}
