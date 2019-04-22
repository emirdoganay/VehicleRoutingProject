import java.util.ArrayList;

public class Truck {
	
	public int Capacity;
	public int usedCap;
	public ArrayList<Integer> route = new ArrayList<>(); //route that trucks follows
 	public ArrayList<Double> distance = new ArrayList<>(); // distance between nodes that visited with order
	public double totaldist;
	
	Truck(){
		
	}
	Truck(int Capacity){
		this.Capacity=Capacity;
		usedCap = 0;
	}
	public void addCap(int x){//to increase used cap
		usedCap=usedCap+x;
	}
	public void TotalDistance(){//calculates total distance traveled by truck
		double buffer=0;//to prevent totaldist to be summed all the time for calling this function
		for(int i = 0; i<distance.size();i++)
			buffer= buffer +distance.get(i);
		
		totaldist=buffer;
	}
	public void Distance(Node[] node){
		distance.clear();//clears the distance array, because this function is called for succesive times, this prevents for extending distance array for nothing
		distance.add(node[0].eclDistance(node[route.get(0)]));
		for(int i=1;i<route.size();i++)
			distance.add(node[route.get(i-1)].eclDistance(node[route.get(i)]));
		distance.add(node[route.get(route.size()-1)].eclDistance(node[0]));
		TotalDistance();//calculates totaldistance for every change in distance node
	}
	public void printInfo(int truckno){ //to print information about truck, we didn't save the # of truck in object so we take is as input
		System.out.printf("Truck #%2d | Used Capacity: %3d | Route [ Depot , %2d,",truckno,usedCap,route.get(0));
		for(int i=1;i<route.size();i++)
			System.out.printf(" %2d,",route.get(i));
		System.out.printf(" Depot] | Total Distance: %.2f %n",totaldist);
		
	}
	public void calculateUsedCap(Node[]node){ //calculates the Used capacity for truck, uses the demand of the customer it serves
		int buffer=0;
		for(int i=0;i<route.size();i++)
			buffer=buffer+node[route.get(i)].demand;
		usedCap=buffer;
	}
}//end of Truckn
