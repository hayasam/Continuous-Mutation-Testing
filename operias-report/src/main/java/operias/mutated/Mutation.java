package operias.mutated;

public class Mutation{

	private String name;
	private String description;
	private String status;
	
	public Mutation(String name, String description, String status){
		 this.name = name;
		 this.description = description;
		 this.setStatus(status);
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}
