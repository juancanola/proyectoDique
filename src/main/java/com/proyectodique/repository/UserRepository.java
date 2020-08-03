package com.proyectodique.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectodique.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	//List<User> findByName(String name);
	public Optional<User> findByName(String name);
	
	public List<User> findAll();
	

}
