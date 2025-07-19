package com.nonononoki.alovoa.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "captcha", schema = "public")
public class Captcha {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(columnDefinition="text")
	private String image;
	
	@JsonIgnore
	private String text;
	
	@JsonIgnore
	private Date date;
	
	@JsonIgnore
	@Column(name="hashcode", unique=true, nullable=false)
	private String hashCode;
}
