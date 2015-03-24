package Utilities;

public class Triple<A, B, C> {
	private A first;
	private B second;
	private C third;
	
	public Triple(A first, B second, C third){
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public boolean equals(Object other) {
    	if (other instanceof Triple) {
    		Triple<?, ?, ?> otherT = (Triple<?, ?, ?>) other;
    		return 
    		((this.first == otherT.first || ( this.first != null && otherT.first != null && this.first.equals(otherT.first)))
    		&& (this.second == otherT.second || ( this.second != null && otherT.second != null && this.second.equals(otherT.second))
    		&& (this.third == otherT.third || ( this.third != null && otherT.third != null && this.third.equals(otherT.third)))));
    	}
    	return false;
    }
	
	public String toString()
    { 
           return "(" + first + ", " + second + ", " + third + ")"; 
    }

    public A getFirst() {
    	return first;
    }

    public void setFirst(A first) {
    	this.first = first;
    }

    public B getSecond() {
    	return second;
    }

    public void setSecond(B second) {
    	this.second = second;
    }
    
    public C getThird(){
    	return this.third;
    }
    
    public void setThird(C third){
    	this.third = third;
    }
    
}
