
public class Node {
	public int x,y,demand,whichtruck;
	public int state;

	Node(){

	}
	Node(int x , int y){
		this.x = x;
		this.y = y;
	}

	public double eclDistance(Node other){//calculates the distance of nodes from each other
		double deltaX= x-other.x;
		double deltaY= y-other.y;
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}


}//end of Node
