package com.proyectodique.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyectodique.entity.Packet;

@Repository
public interface PacketRepository extends JpaRepository<Packet, Long> {

	
	public List<Packet> findAll();
	
	 @Query(value = "select type,count(type),time FROM packets group by type,time", nativeQuery = true)
	 public List<Object[]> findByTimeType();
	 
	

}
