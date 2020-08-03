package com.proyectodique.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.proyectodique.capturador.Capturadora;
import com.proyectodique.entity.User;
import com.proyectodique.repository.UserRepository;


@Controller
@RequestMapping("/")
public class UserController {

	@Autowired
	private UserRepository userRepository;	
	
	@GetMapping("showForm")
	public String showUserForm(User user) {
		return "add-user";
	}
	
	@GetMapping("users")
	public String users(Model model) {
		model.addAttribute("users", this.userRepository.findAll());
		return "users";
	}
	
	@PostMapping("add")
	public String addUser(@Valid User user, BindingResult result, Model model) {
		if(result.hasErrors()) {
			return "add-user";
		}
		
		this.userRepository.save(user);
		return "redirect:/users";
	}
	
	
	@GetMapping("edit/{id}")
	public String showUpdateForm(@PathVariable ("id") long id, Model model) {
		User user = this.userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid user id : " + id));
		
		model.addAttribute("user", user);
		return "update-user";
	}
	
	@PostMapping("update/{id}")
	public String updateUser(@PathVariable("id") long id, @Valid User user, BindingResult result, Model model) {
		if(result.hasErrors()) {
			user.setId(id);
			return "update-user";
		}
		
		// update student
		userRepository.save(user);
		
		// get all students ( with update)
		model.addAttribute("users", this.userRepository.findAll());
		return "redirect:/users";
	}
	
	@GetMapping("delete/{id}")
	public String deleteUser(@PathVariable ("id") long id, Model model) {
		
		User user = this.userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid user id : " + id));
		
		this.userRepository.delete(user);
		model.addAttribute("users", this.userRepository.findAll());
		return "redirect:/users";
		
	}
	
	
	@GetMapping({"/","/login"})
	public String index() {
		return "index";
	}
	

	
	
	@GetMapping("/admin")
	public String admin() {
		return "admin";
	}
	
	@PostMapping("/logout")
	public String logout() {
		return "index";
	}
	
	@GetMapping("/listar")
    public String listar() {
        return "listusers";
    }
	
	//Servicios
	@RequestMapping(value = "findall", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<User>> findAll() {
		try {
			return new ResponseEntity<List<User>>(userRepository.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<User>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	@RequestMapping(value = "agregar", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public void agregarUser(@RequestBody User user, HttpServletResponse hsr) throws IOException{
		this.userRepository.save(user);
	}*/
	
	@RequestMapping(value = "agregar", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> agregarusuarioPrueba() {
	
		try {
			User user=new User("1","juan");
			this.userRepository.save(user);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
	
	}
	
	
}
