package CGG.java_callgraph.javacg.stat;

public class CheckMapResult {

	String methodString;
	boolean success;
	String mavenDependency;

	public void setMethodString(String methodString) {
		this.methodString = methodString;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMavenDependency() {
		return mavenDependency;
	}

	public void setMavenDependency(String mavenDependency) {
		this.mavenDependency = mavenDependency;
	}

	public CheckMapResult(String methodString, boolean success, String mavenDependency) {

		this.methodString = methodString;
		this.success = success;
		this.mavenDependency = mavenDependency;
	}

	public CheckMapResult(String methodString, boolean success) {
		this.methodString = methodString;
		this.success = success;
	}
	
	public String getMethodString() {
		return this.methodString;
	}
	public boolean getSuccess() {
		return this.success;
	}
	
}
