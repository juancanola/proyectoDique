package com.proyectodique.capturador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.springframework.beans.factory.annotation.Autowired;

import com.proyectodique.entity.Packet;
import com.proyectodique.entity.PacketGrafica;
import com.proyectodique.repository.PacketRepository;
import com.proyectodique.util.UtilidadesJuan;

import org.pcap4j.core.PcapPacket;
import org.pcap4j.core.Pcaps;

import cic.cs.unb.ca.jnetpcap.BasicFlow;
import cic.cs.unb.ca.jnetpcap.BasicPacketInfo;
import cic.cs.unb.ca.jnetpcap.FlowGenerator;
import cic.cs.unb.ca.jnetpcap.PacketReader;


public class Capturadora {

	static String directoryPcap = "/home/test/dump/muralla.pcap";

	static PacketRepository packetRepositoryCapt;
	static PcapHandle handle = null;
	
	public static void capturar(PacketRepository packetRepository) throws IOException, PcapNativeException, NotOpenException,Exception {
		
		packetRepositoryCapt = packetRepository;
	
		//Obtener interfaces
		List<PcapNetworkInterface> allDevs = null;
		try {
		    allDevs = Pcaps.findAllDevs();
		} catch (PcapNativeException e) {
		    throw new IOException(e.getMessage());
		}
		
		if (allDevs == null || allDevs.isEmpty()) {
		    throw new IOException("No NIF to capture.");
		}
		
		//Obtener interfaz 0 que en este caso es la wan
		PcapNetworkInterface device = allDevs.get(0);
        if (device == null) {
            System.out.println("No device chosen.");
            System.exit(1);
        }

        // Open the device and get a handle
        int snapshotLength = 65536; // in bytes   
        int readTimeout = 1; // in milliseconds                   
       
        handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
        
        //Donde se almacanera el archivo
        PcapDumper dumper  = handle.dumpOpen(directoryPcap);
        
        

        // Create a listener that defines what to do with the received packets
        PacketListener listener = new PacketListener() {
        	int packetNumber = 0;
            @Override
            public void gotPacket(PcapPacket packet) {

            	if(packet!=null) 
            	{
                // Override the default gotPacket() function and process packet
            	
                //System.out.println("Packet No. "+packet.getPayload());
               System.out.println("========PAQUETE NRO: "+packetNumber);
                packetNumber++;
                
                Thread thread = new Thread(){
                    public void run(){
                        try {
        					extraerFeatures(packet,packetNumber);
        					return;
        				} catch (PcapNativeException | NotOpenException e1) {
        					// TODO Auto-generated catch block
        					//e1.printStackTrace();
        				} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
                    }
                  };
//
                  thread.start();
        
                
                
		        //Write packets as needed
		        try {
		            dumper.dump(packet);
		        } catch (NotOpenException e) {
		            e.printStackTrace();
		        }
      
            }
        }
        };

		try {
			// Cantidad de paquetes
			int maxPackets = 1000000;
			handle.loop(maxPackets, listener);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		// Cleanup when complete
		dumper.close();
		handle.close();
		
		 // System.out.println("FINISH");
	
}
	
	public static void detener() {
		try {
			handle.breakLoop();
		} catch (NotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private static synchronized void extraerFeatures(PcapPacket packet,int numberPacket) throws PcapNativeException, NotOpenException, IOException,Exception {
		
		String tmppcap = "/home/test/dump/tempmuralla"+numberPacket+".pcap";
		               
       

		PcapDumper dumperTmp  = handle.dumpOpen(tmppcap);

		
		
	       
		//Convertir a hexadecimal
        //String packetHex = packet.toHexString();
        //System.out.println("Paquete: "+packetHex);
        //tempFile(packetHex);
        dumperTmp.dump(packet);
        dumperTmp.close();
        PacketReader packetReader = new PacketReader(tmppcap);
        
		BasicPacketInfo packetInfo = packetReader.nextPacket();
		BasicFlow basicFlow = new BasicFlow(packetInfo);
		SummaryStatistics flowIAT = new SummaryStatistics();
		//flowIAT.addValue(packet.getTimestamp()-this.flowLastSeen);
		//flowIAT = basicFlow.getFlowIAT();
		
		String srcIP = packetInfo.getSourceIP();
		int srcPort = packetInfo.getSrcPort();
		String dstIP = packetInfo.getDestinationIP();
		
		int dstPort = packetInfo.getDstPort();
		int protocol = packetInfo.getProtocol();
		
		long totalFwdPackets = basicFlow.getTotalFwdPackets();
		long totalBwdPackets = basicFlow.getTotalBackwardPackets();
		
		double totalLenghtFwdPackets = basicFlow.getTotalLengthofFwdPackets();
		double totalLenghtBwdPackets = basicFlow.getTotalLengthofBwdPackets();
		
		long flowDuration = basicFlow.getFlowDuration();
		
		
		double fwdPacketLengthMax = basicFlow.getFwdPacketLengthMax();
		double fwdPacketLenghtMin = basicFlow.getFwdPacketLengthMin();
		double fwdPacketLenghtStd = basicFlow.getFwdPacketLengthStd();
		double fwdPacketLenghtMean = basicFlow.getFwdPacketLengthMean();
		
		double bwdPacketLengthMax = basicFlow.getBwdPacketLengthMax();
		double bwdPacketLenghtMin = basicFlow.getBwdPacketLengthMin();
		double bwdPacketLenghtStd = basicFlow.getBwdPacketLengthStd();
		double bwdPacketLenghtMean = basicFlow.getBwdPacketLengthMean();
		
//		double flowBytes = basicFlow.getFlowBytesPerSec();
//		double flowPackets = basicFlow.getFlowPacketsPerSec();
//		
//		double flowIATMean = basicFlow.getFlowIAT().getMean();
//		double flowIATStd = basicFlow.getFlowIAT().getStandardDeviation();
//		double flowIATMax = basicFlow.getFlowIAT().getMax();
//		double flowIATMin = basicFlow.getFlowIAT().getMin();
		
		double fwdIATTotal = basicFlow.getFwdIATTotal();
		double fwdIATMean = basicFlow.getFwdIATMean();
		double fwdIATStd = basicFlow.getFwdIATStd();
		double fwdIATMax = basicFlow.getFwdIATMax();
		double fwdIATMin = basicFlow.getFwdIATMin();
		
		double bwdIATTotal = basicFlow.getBwdIATTotal();
		double bwdIATMean = basicFlow.getBwdIATMean();
		double bwdIATStd = basicFlow.getBwdIATStd();
		double bwdIATMax = basicFlow.getBwdIATMax();
		double bwdIATMin = basicFlow.getBwdIATMin();
		
		double fwdPSHFlag = basicFlow.getFwdPSHFlags();
		double bwdPSHFlag = basicFlow.getBwdPSHFlags();
		
		double fwdURGFlag = basicFlow.getFwdURGFlags();
		double bwdURGFlag = basicFlow.getBwdURGFlags();
		double fwdHeaderLength = basicFlow.getFwdHeaderLength();
		double bwdHeaderLength = basicFlow.getBwdHeaderLength();
		
		double fwdPacketPerSec = basicFlow.getfPktsPerSecond();
		double bwdPacketPerSec = basicFlow.getbPktsPerSecond();
		
		double minPacketLenght = basicFlow.getMinPacketLength();
		double maxPacketLenght = basicFlow.getMaxPacketLength();
		double meanPacketLenght = basicFlow.getPacketLengthMean();
		double stdPacketLenght = basicFlow.getPacketLengthStd();
		double variancePacketLenght = basicFlow.getPacketLengthVariance();
		

		int flagFIN = packetInfo.hasFlagFIN()?1:0;
		int flagSYN = packetInfo.hasFlagSYN()?1:0;
		int flagRST = packetInfo.hasFlagRST()?1:0;
		int flagPSH = packetInfo.hasFlagPSH()?1:0;
		int flagACK = packetInfo.hasFlagACK()?1:0;
		int flagURG = packetInfo.hasFlagURG()?1:0;
		int flagCWR = packetInfo.hasFlagCWR()?1:0;
		int flagECE = packetInfo.hasFlagECE()?1:0;
		
		double downUpRatio = basicFlow.getDownUpRatio();
		double avgPacketSize = basicFlow.getAvgPacketSize();
		
		double fwdAVGSegmentSize = basicFlow.fAvgSegmentSize();
		double bwdAVGSegmentSize = basicFlow.bAvgSegmentSize();
		
		double fwdHeaderLength1 = basicFlow.getFwdHeaderLength();
		
		double fAvgBytesPerBulk = basicFlow.fAvgBytesPerBulk();
		double fAvgPacketsPerBulk = basicFlow.fAvgPacketsPerBulk();
		double fAvgBulkRate = basicFlow.fAvgBulkRate();
		
		double bAvgBytesPerBulk = basicFlow.bAvgBytesPerBulk();
		double bAvgPacketsPerBulk = basicFlow.bAvgPacketsPerBulk();
		double bAvgBulkRate = basicFlow.bAvgBulkRate();
		
		double sflow_fbytes = basicFlow.getSflow_fbytes();
		double sflow_fpackets = basicFlow.getSflow_fpackets();
		
		double sflow_bbytes = basicFlow.getSflow_bbytes();
		double sflow_bpackets = basicFlow.getSflow_bpackets();
		
		double init_Win_bytes_forward = basicFlow.getInit_Win_bytes_forward();
		double init_Win_bytes_backward = basicFlow.getInit_Win_bytes_backward();
		
		double act_data_pkt_forward= basicFlow.getAct_data_pkt_forward();
		double min_seg_size_forward = basicFlow.getmin_seg_size_forward();
		
		double activeMean = basicFlow.getActiveMean();
		double activeSTD = basicFlow.getActiveStd();
		double activeMin = basicFlow.getActiveMin();
		double activeMax = basicFlow.getActiveMax();
		
		double idleMean = basicFlow.getIdleMean();
		double idleStd = basicFlow.getIdleStd();
		double idleMax = basicFlow.getIdleMax();
		double idleMin = basicFlow.getIdleMin();
		
		String time = getTime();
		
		
//		System.out.println("============PACKET " +numberPacket+"====================");
//		System.out.println("dstPort "+dstPort);
//		System.out.println(" protocol "+ protocol);
//		System.out.println(" totalFwdPackets "+ totalFwdPackets);
//		System.out.println(" totalBwdPackets "+ totalBwdPackets);
//		System.out.println(" totalLenghtFwdPackets "+ totalLenghtFwdPackets);
//		System.out.println(" totalLenghtBwdPackets "+ totalLenghtBwdPackets);
//		System.out.println(" flowDuration "+ flowDuration);
//		System.out.println(" fwdPacketLengthMax "+ fwdPacketLengthMax);
//		System.out.println(" fwdPacketLenghtMin "+ fwdPacketLenghtMin);
//		System.out.println(" fwdPacketLenghtStd "+ fwdPacketLenghtStd);
//		System.out.println(" fwdPacketLenghtMean "+ fwdPacketLenghtMean);
//		System.out.println(" bwdPacketLengthMax "+ bwdPacketLengthMax);
//		System.out.println(" bwdPacketLenghtMin "+ bwdPacketLenghtMin);
//		System.out.println(" bwdPacketLenghtStd "+ bwdPacketLenghtStd);
//		System.out.println(" bwdPacketLenghtMean "+ bwdPacketLenghtMean);
////		System.out.println(" flowBytes "+ flowBytes);
////		System.out.println(" flowPackets "+ flowPackets);
////		System.out.println(" flowIATMean "+ flowIATMean);
////		System.out.println(" flowIATStd "+ flowIATStd);
////		System.out.println(" flowIATMax "+ flowIATMax);
////		System.out.println(" flowIATMin "+ flowIATMin);
//		System.out.println(" fwdIATTotal "+ fwdIATTotal);
//		System.out.println(" fwdIATMean "+ fwdIATMean);
//		System.out.println(" fwdIATStd "+ fwdIATStd);
//		System.out.println(" fwdIATMax "+ fwdIATMax);
//		System.out.println(" fwdIATMin "+ fwdIATMin);
//		System.out.println(" bwdIATTotal "+ bwdIATTotal);
//		System.out.println(" bwdIATMean "+ bwdIATMean);
//		System.out.println(" bwdIATStd "+ bwdIATStd);
//		System.out.println(" bwdIATMax "+ bwdIATMax);
//		System.out.println(" bwdIATMin "+ bwdIATMin);
//		System.out.println(" fwdPSHFlag "+ fwdPSHFlag);
//		System.out.println(" bwdPSHFlag "+ bwdPSHFlag);
//		System.out.println(" fwdURGFlag "+ fwdURGFlag);
//		System.out.println(" bwdURGFlag "+ bwdURGFlag);
//		System.out.println(" fwdHeaderLength "+ fwdHeaderLength);
//		System.out.println(" bwdHeaderLength "+ bwdHeaderLength);
//		System.out.println(" fwdPacketPerSec "+ fwdPacketPerSec);
//		System.out.println(" bwdPacketPerSec "+ bwdPacketPerSec);
//		System.out.println(" minPacketLenght "+ minPacketLenght);
//		System.out.println(" maxPacketLenght "+ maxPacketLenght);
//		System.out.println(" meanPacketLenght "+ meanPacketLenght);
//		System.out.println(" stdPacketLenght "+ stdPacketLenght);
//		System.out.println(" variancePacketLenght "+ variancePacketLenght);
//		System.out.println(" flagFIN  "+ flagFIN );
//		System.out.println(" flagSYN  "+ flagSYN );
//		System.out.println(" flagRST "+ flagRST);
//		System.out.println(" flagPSH "+ flagPSH);
//		System.out.println(" flagACK "+ flagACK);
//		System.out.println(" flagURG "+ flagURG);
//		System.out.println(" flagCWR "+ flagCWR);
//		System.out.println(" flagECE "+ flagECE);
//		System.out.println(" downUpRatio "+ downUpRatio);
//		System.out.println(" avgPacketSize "+ avgPacketSize);
//		System.out.println(" fwdAVGSegmentSize "+ fwdAVGSegmentSize);
//		System.out.println(" bwdAVGSegmentSize "+ bwdAVGSegmentSize);
//		System.out.println(" fwdHeaderLength1 "+ fwdHeaderLength1);
//		System.out.println(" fAvgBytesPerBulk "+ fAvgBytesPerBulk);
//		System.out.println(" fAvgPacketsPerBulk "+ fAvgPacketsPerBulk);
//		System.out.println(" fAvgBulkRate "+ fAvgBulkRate);
//		System.out.println(" bAvgBytesPerBulk "+ bAvgBytesPerBulk);
//		System.out.println(" bAvgPacketsPerBulk "+ bAvgPacketsPerBulk);
//		System.out.println(" bAvgBulkRate "+ bAvgBulkRate);
//		System.out.println(" sflow_fbytes "+ sflow_fbytes);
//		System.out.println(" sflow_fpackets "+ sflow_fpackets);
//		System.out.println(" sflow_bbytes "+ sflow_bbytes);
//		System.out.println(" sflow_bpackets "+ sflow_bpackets);
//		System.out.println(" init_Win_bytes_forward "+ init_Win_bytes_forward);
//		System.out.println(" init_Win_bytes_backward "+ init_Win_bytes_backward);
//		System.out.println(" act_data_pkt_forward "+ act_data_pkt_forward);
//		System.out.println(" min_seg_size_forward "+ min_seg_size_forward);
//		System.out.println(" activeMean "+ activeMean);
//		System.out.println(" activeSTD "+ activeSTD);
//		System.out.println(" activeMin "+ activeMin);
//		System.out.println(" activeMax "+ activeMax);
//		System.out.println(" idleMean "+ idleMean);
//		System.out.println(" idleStd "+ idleStd);
//		System.out.println(" idleMax "+ idleMax);
//		System.out.println(" idleMin "+ idleMin);
//		System.out.println(" TIME "+ time);


		

		String linea = dstPort +","+
				 protocol +","+
				 totalFwdPackets +","+
				 totalBwdPackets +","+
				 totalLenghtFwdPackets +","+
				 totalLenghtBwdPackets +","+
				 flowDuration +","+
				 fwdPacketLengthMax +","+
				 fwdPacketLenghtMin +","+
				 fwdPacketLenghtStd +","+
				 fwdPacketLenghtMean +","+
				 bwdPacketLengthMax +","+
				 bwdPacketLenghtMin +","+
				 bwdPacketLenghtStd +","+
				 bwdPacketLenghtMean +","+
				 fwdIATTotal +","+
				 fwdIATMean +","+
				 fwdIATStd +","+
				 fwdIATMax +","+
				 fwdIATMin +","+
				 bwdIATTotal +","+
				 bwdIATMean +","+
				 bwdIATStd +","+
				 bwdIATMax +","+
				 bwdIATMin +","+
				 fwdPSHFlag +","+
				 bwdPSHFlag +","+
				 fwdURGFlag +","+
				 bwdURGFlag +","+
				 fwdHeaderLength +","+
				 bwdHeaderLength +","+
				 fwdPacketPerSec +","+
				 bwdPacketPerSec +","+
				 minPacketLenght +","+
				 maxPacketLenght +","+
				 meanPacketLenght +","+
				 stdPacketLenght +","+
				 variancePacketLenght +","+
				 flagFIN  +","+
				 flagSYN  +","+
				 flagRST +","+
				 flagPSH +","+
				 flagACK +","+
				 flagURG +","+
				 flagCWR +","+
				 flagECE +","+
				 downUpRatio +","+
				 avgPacketSize +","+
				 fwdAVGSegmentSize +","+
				 bwdAVGSegmentSize +","+
				 fwdHeaderLength1 +","+
				 fAvgBytesPerBulk +","+
				 fAvgPacketsPerBulk +","+
				 fAvgBulkRate +","+
				 bAvgBytesPerBulk +","+
				 bAvgPacketsPerBulk +","+
				 bAvgBulkRate +","+
				 sflow_fbytes +","+
				 sflow_fpackets +","+
				 sflow_bbytes +","+
				 sflow_bpackets +","+
				 init_Win_bytes_forward +","+
				 init_Win_bytes_backward +","+
				 act_data_pkt_forward +","+
				 min_seg_size_forward +","+
				 activeMean +","+
				 activeSTD +","+
				 activeMin +","+
				 activeMax +","+
				 idleMean +","+
				 idleStd +","+
				 idleMax +","+
				 idleMin+","+0;

			

		File totalFile = sobreEscribirArchivo(linea,numberPacket);
		boolean packetType = predecir(totalFile,numberPacket,srcIP);
		
		
		//System.out.println("==========END=PACKET "+numberPacket+"====================");
		
		if((srcPort!=0)&&(dstPort!=0)
				//&&(srcPort!=80)
				)
		{
		//INSERTAR EN LA BD
		com.proyectodique.entity.Packet packetMuralla = new com.proyectodique.entity.Packet(
								srcIP,
								String.valueOf(srcPort),
								dstIP,	
								String.valueOf(dstPort),
								String.valueOf(protocol),
								packetType,
								time);
		packetRepositoryCapt.save(packetMuralla);
		


		}
		
		borrarArchivosTemporales(numberPacket);

		
	}
	
	private static File sobreEscribirArchivo(String linea,int numberPacket) {
		String tmpcsv =  "/home/test/dump/tempmuralla"+numberPacket+".csv";
		File myObj=new File(tmpcsv);;
	    try {
		      if (myObj.createNewFile()) {
		        //System.out.println("File created: " + myObj.getName());
		      } else {
		       // System.out.println("File already exists.");
		      }
		      FileWriter myWriter = new FileWriter(tmpcsv);
		      myWriter.write(linea);
		      myWriter.close();
		     // System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		
		    }
		return myObj;
	    
		
	}
	
	private static void borrarArchivosTemporales(int numberPacket) {
		String tmppcap =  "/home/test/dump/tempmuralla"+numberPacket+".pcap";
		String tmpcsv =  "/home/test/dump/tempmuralla"+numberPacket+".csv";
		File mypcap=new File(tmppcap);
		File mycsv=new File(tmpcsv);
	    mypcap.delete();
		mycsv.delete();
		//  System.out.println("Successfully delete to the file.");
	    
		
	}

	private static boolean predecir(File totalFile, int numberPacket, String srcIP) {
		
		boolean packetType = false;
		RecordReader recordReader = new CSVRecordReader(0,",");
		try {
			recordReader.initialize(new FileSplit(totalFile));
			
			DataSetIterator iteratorTest = new RecordReaderDataSetIterator(recordReader,1,22,2);
	        DataSet dataTest = iteratorTest.next();
	        
			
			 //LOAD MODEL
	        File location = new File("/home/test/dump/Model50FNNDOS2019.zip");

	        //Create the model
	        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(location, false);
	        
	        //evaluate the model on the test set
	        Evaluation eval = new Evaluation(2);
	        INDArray output = model.output(dataTest.getFeatures());
//	        System.out.println("***********************************");
	      //  System.out.println(output);
	       // System.out.println("La probabilidad es de "+output.maxNumber());
	        Double value = (Double) output.maxNumber();
	        for (int j = 0; j < 2; j++) {
	        	if(value==output.getDouble(j))
	        	{
	        		if(srcIP.equals("192.168.1.3")) {
	        			 packetType = false;
	        			break;
	        		}
	        		if(j==0)
	        		{
	        		 System.out.println("El paquete "+numberPacket+" es NORMAL "+j);
	        		 packetType = false;
	        		}
	        		else if(j==1)
	        		{
	        	    System.out.println("El paquete "+numberPacket+" es MALIGNO "+j);
	        	    packetType = true;
	        	    
	        		if(UtilidadesJuan.modoApp) { //SI ESTA MODO IPS CREAR REGLA
	        			
	        			ejecutarInstruccion(srcIP);
	        			//System.out.println("MODO IPS");
	        			
	        		}
	        		else {
	        			
	        			//System.out.println("MODO IDS");
	        		}
	        	    
	        		}
	        			
	        		
	        	}
	        	
			}
	      //  System.out.println("***********************************");
	        
	        eval.eval(dataTest.getLabels(), output);
	      
			
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block

		}
		return packetType;
		
	}

	private static String getTime() {
		   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		   LocalDateTime now = LocalDateTime.now();  

		   
		   return dtf.format(now);
		   
//		      Date date = new Date();
//		      //This method returns the time in millis
//		      return String.valueOf(date.getTime());
		
	}
	
	private static void ejecutarInstruccion(String srcIP) {
		
	
		        String s;
		        Process p;
		        try {
		          //  p = Runtime.getRuntime().exec("ufw deny out from any to "+srcIP);
		            p = Runtime.getRuntime().exec("ufw deny from "+srcIP+" to any");
		            BufferedReader br = new BufferedReader(
		                new InputStreamReader(p.getInputStream()));
		            while ((s = br.readLine()) != null)
		                System.out.println("line: " + s);
		            p.waitFor();
		            System.out.println ("exit: " + p.exitValue());
		            p.destroy();
		        } catch (Exception e) {}
		    
		
	}
	
//    public static long insertarPaquete(PacketRepository packetRepository, PacketReader packetReaderGlobal) {
//    	
//		BasicPacketInfo packetInfo=null;
//
//		try {
//		packetInfo = packetReaderGlobal.nextPacket();
//		}
//		catch(Exception e) {
//
//		}
//		if(packetInfo==null)
//			return 0;
//		String srcIP = packetInfo.getSourceIP();
//		int srcPort = packetInfo.getSrcPort();
//		String dstIP = packetInfo.getDestinationIP();
//		
//		int dstPort = packetInfo.getDstPort();
//		int protocol = packetInfo.getProtocol();
//		
//		boolean packetType = true;
//		String time = getTime();
//    	
//		//INSERTAR EN LA BD
//		com.proyectomuralla.entity.Packet packetMuralla = new com.proyectomuralla.entity.Packet(
//								srcIP,
//								String.valueOf(srcPort),
//								dstIP,	
//								String.valueOf(dstPort),
//								String.valueOf(protocol),
//								packetType,
//								time);
//		packetRepository.save(packetMuralla);
//		
//		return 1;
//    	
//    	
//    }

}