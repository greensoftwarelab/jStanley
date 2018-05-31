package greenlab.models;

public class Suggestion {
	
	private String name;
	private InvocationCost cost;
	
	public Suggestion(String name, InvocationCost cost) {
		super();
		this.setName(name);
		this.setCost(cost);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InvocationCost getCost() {
		return cost;
	}

	public void setCost(InvocationCost cost) {
		this.cost = cost;
	}
	
	
}
