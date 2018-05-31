package greenlab.models;

public class InvocationCost {
	
	private String type;
	private String method;
	private float joules;
	private float ms;
	private float mb;
	private boolean real;
	private float normalJoules;
	private float normalMs;
	private float normalMb;
	
	public InvocationCost(String type, String method, float joules, float ms, float mb) {
		this.type = type;
		this.method = method;
		this.joules = joules;
		this.ms = ms;
		this.mb = mb;
		this.real = false;
		this.normalJoules = joules;
		this.normalMs = ms;
		this.normalMb = mb;
	}
	
	public InvocationCost(String type, String method, float joules, float ms, float mb, boolean real) {
		this.type = type;
		this.method = method;
		this.joules = joules;
		this.ms = ms;
		this.mb = mb;
		this.real = real;
		this.normalJoules = joules;
		this.normalMs = ms;
		this.normalMb = mb;
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public String getType() {
		return this.type;
	}

	public float getJoules() {
		return this.joules;
	}

	public float getMs() {
		return this.ms;
	}
	
	public float getMb() {
		return this.mb;
	}

	public float getNormalJoules() {
		return this.normalJoules;
	}

	public float getNormalMs() {
		return this.normalMs;
	}
	
	public float getNormalMb() {
		return this.normalMb;
	}
	
	public boolean isReal() {
		return this.real;
	}

	public void setNormalJoules(float j) {
		this.normalJoules = j;
	}
	
	public void setNormalMs(float m) {
		this.normalMs = m;
	}
	
	public void setNormalMb(float m) {
		this.normalMb = m;
	}
}
