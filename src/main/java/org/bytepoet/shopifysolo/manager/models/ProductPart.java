package org.bytepoet.shopifysolo.manager.models;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity
public class ProductPart {

	@Id
	@JsonProperty
	private UUID id = UUID.randomUUID();
	
	@JsonIgnore
	@OneToMany(mappedBy="productPart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<ProductPartDistribution> distributions;
	
	@JsonProperty
	private int quantity;
	
	@JsonProperty
	private String description;
	
	@JsonProperty
	private String link;
	
	@JsonProperty
	private String alternativeLink;
	
	@JsonProperty
	private String alternativeDescription;
	
	@JsonProperty
	private String alternativeLink2;
	
	@JsonProperty
	private String alternativeDescription2;

	public int getQuantity() {
		return quantity;
	}
	
	
	
	
	
}
