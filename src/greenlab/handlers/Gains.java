package greenlab.handlers;

/**
 * This class serves to store information about the gains of a suggested collection
 * @author jacome
 *
 */
public class Gains {
	
	private float mb;
	private float ms;
	private float joules;
	
	
	public Gains(float mb, float ms, float joules) {
		this.mb = mb;
		this.ms = ms;
		this.joules = joules;
	}


	public Gains() {
		this.mb = -1;
		this.ms = -1;
		this.joules = -1;
	}


	public float getMb() {
		return mb;
	}


	public void setMb(float mb) {
		this.mb = mb;
	}


	public float getMs() {
		return ms;
	}


	public void setMs(float ms) {
		this.ms = ms;
	}


	public float getJoules() {
		return joules;
	}


	public void setJoules(float joules) {
		this.joules = joules;
	}
	
	

}
