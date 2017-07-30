/**
Contributors: Nachi
 */
package com.fj.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fj.type.Either;
import com.fj.type.Either.Left;
import com.fj.type.Either.Right;

public class InventorySample {

	Map<String,List<String>> productDetails;
	Map<String,Component> components;
	Map<String,Double> prices;
	Double discount;

	public static class BikeComponent implements Component{

		String id;
		
		public BikeComponent(String id){
			this.id = id;
		}
		
		@Override
		public String prettyResult() {
			return "Bike:"+id;
		}

		@Override
		public String getId() {
			return id;
		}
		
	}
	
	public InventorySample(Map<String,List<String>> productDetails, Map<String,Component> components,
			Map<String,Double> prices, Double discount){
		this.productDetails = productDetails;
		this.components = components;
		this.prices = prices;
		this.discount = discount;
	}

	public interface Component{
		String prettyResult();
		String getId();
	}

	public Either<Exception, Component> getComponent(String componentName){
		Optional<Component> optionalComponent = Optional.ofNullable(components.get(componentName));
		return optionalComponent
				.<Either<Exception,Component>>map(c -> new Right<>(c))
				.orElse(new Left<>(new Exception("Component not found: " + componentName)));
	}

	public Either<Exception,Double> findOriginalPrice(String componentId){
		Optional<Double> optionalPrice = Optional.ofNullable(prices.get(componentId));
		return optionalPrice
				.<Either<Exception,Double>>map(price -> new Right<>(price))
				.orElse(new Left<>(new Exception("Price not found for " + componentId)));
	}

	//simple local fun
	public List<String> getComponentNames(String product){
		return productDetails.get(product);
	}


	public String print(Either<Exception, Component> componentResp){
		return componentResp.match(
				(Exception ex) -> "Failed because " + ex.getMessage(), 
				(Component p) -> "Component info: " + p.prettyResult());
	}

	public Double discountedPrice(Double originalPrice){
		Optional<Double> optionalDiscount = Optional.ofNullable(discount);
		return optionalDiscount
				.map(discount -> originalPrice - originalPrice * discount)
				.orElse(originalPrice);
	}

	public Either<Exception,Double> getComponentPrice(Either<Exception, Component> response){
		return response
				.fmap(result -> result.getId())
				.bind(this::findOriginalPrice)
				.fmap(this::discountedPrice);
	}

	public Either<Exception,Double> getProductPrice(String product){
		return getComponentNames(product)
				.stream()
				.map(this::getComponent)
				.map(this::getComponentPrice)
				.reduce((x,y)-> x.bind(xPrice -> y.bind(yPrice -> new Right<>(xPrice + yPrice))))
				.get();
	}

	public static void main(String args[]){
		//bike product
		Map<String,List<String>> productDetails = new HashMap<>();
		List<String> bikeComponentNames = new ArrayList<>();
		bikeComponentNames.add("wheel");
		bikeComponentNames.add("wheel");
		bikeComponentNames.add("frame");
		productDetails.put("bike", bikeComponentNames);

		//componets
		Map<String,Component> components = new HashMap<>();
		components.put("wheel", new BikeComponent("mrf#123"));
		components.put("frame", new BikeComponent("cf#52"));

		//component prices
		Map<String,Double> prices = new HashMap<>();
		prices.put("mrf#123", 100D);
		prices.put("cf#52", 1000D);

		Double discount = 0.10;

		Either<Exception, Double> ep = new InventorySample(productDetails, components, prices, discount).getProductPrice("bike");

		ep.match((Exception e) -> {System.out.println(e.getMessage()); return null;}, 
				(Double price) -> {System.out.println(price); return null;});

	}

}
