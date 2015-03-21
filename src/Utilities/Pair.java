package Utilities;

public class Pair<A, B> {
	private A first;
	private B second;
	
	public Pair(A first, B second){
		super();
		this.first = first;
		this.second = second;
	}
	
	public boolean equals(Object other) {
    	if (other instanceof Pair) {
    		Pair<?, ?> otherPair = (Pair<?, ?>) other;
    		return 
    		((this.first == otherPair.first || ( this.first != null && otherPair.first != null && this.first.equals(otherPair.first)))
    		&& (this.second == otherPair.second || ( this.second != null && otherPair.second != null && this.second.equals(otherPair.second))));
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getfirst() {
    	return first;
    }

    public void setfirst(A first) {
    	this.first = first;
    }

    public B getsecond() {
    	return second;
    }

    public void setsecond(B second) {
    	this.second = second;
    }
}
