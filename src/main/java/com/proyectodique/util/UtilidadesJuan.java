package com.proyectodique.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UtilidadesJuan {

	public static boolean modoApp = false;
    public static void main(String ...args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
        //El String que mandamos al metodo encode es el password que queremos encriptar.
	System.out.println(bCryptPasswordEncoder.encode("1234"));
    }
    
    public static String convertirFecha(Object obj) {
    Date date1;
    String fecha = "";
	try {
	//Convertir fecha y restar 5 para que se visualice bien
		date1= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(obj.toString());
		 Calendar cal = Calendar.getInstance(); // creates calendar
		    cal.setTime(date1); // sets calendar time/date
		    cal.add(Calendar.HOUR_OF_DAY, -5); 
		    date1= cal.getTime();
		  //System.out.println(obj.toString()+"\t"+date1);  
		  fecha = String.valueOf(date1.getTime());
		    
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
	return fecha;
    }
}