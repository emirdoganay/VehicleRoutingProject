

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class main {

	public static void main(String[] args) throws FileNotFoundException {	

		int customers, capacity,trucks,alpha=90;//alpha for decreasing heat

		Scanner input = new Scanner(new java.io.File("data3.txt"));

		String temp = input.nextLine();
		String[] token = temp.split(":");
		trucks = Integer.parseInt(token[1]);
		temp = input.nextLine();
		token = temp.split(":");
		capacity = Integer.parseInt(token[1]);
		temp = input.nextLine();
		token = temp.split(":");
		customers = Integer.parseInt(token[1]);
		// as long as initial information, about # of customer, # of trucks are given, code reads according to that
		// txt file format has been corrected. Some misplaced spaces causes problem for reading # of customers and trucks
		
		Node[] mynodes= new Node[customers+1];//creating Node for customers, +1 for depot

		for(int i=0;i<3;i++)//this "for" loop is for passing the unnecessary lines
			input.nextLine();
		for(int i=0;i<customers+1;i++){
			input.nextInt();//passes the node number
			mynodes[i]= new Node(input.nextInt(),input.nextInt());//constructs node with x,y cordinates readed from file
		}

		for(int i=0;i<3;i++)//this "for" loop is for passing the unnecessary lines
			temp = input.nextLine();
		for(int i=0;i<customers+1;i++){
			input.nextInt();//passes the node number
			mynodes[i].demand=input.nextInt();//reads demand
		}
		//so for code reads the information from txt file

		Truck[] mytrucks = new Truck [trucks];//iteration solution
		Truck[] bestsolution = new Truck [trucks];//best solution
		//creating Truck object for best solution, and iteration solutions

		for(int i = 0; i<trucks;i++){//constructing Trucks with capacity
			mytrucks[i]= new Truck(capacity);
			bestsolution[i]=new Truck(capacity);
		}



		initialSolution(mynodes,bestsolution,mytrucks);//creates random generated initial solution
		for(int i=0;i<bestsolution.length;i++){//calls distance formulation for every truck in Truck objects 
			bestsolution[i].Distance(mynodes); //it calculates every distance for ith truck route
			mytrucks[i].Distance(mynodes);
		}

		System.out.println("Initial Random found Solution:");
		for(int i=0;i<bestsolution.length;i++)
			bestsolution[i].printInfo(i+1);
		System.out.printf("Total Distance : %.3f ",objectiveValue(bestsolution));
		//printing initial solution
		
		int[] stop = new int[4];//stop[0] : accepted moves , stop[1] : successive rejected moves, stop[2] : # of iteration
		stop[3]=100; // heat level
		while(stoppingConditions(stop)==1){//checks conditions by heat and # of iteration
			stop[2]++;//increasing iteration number for every iteration
			Truck[] gentruck = new Truck [trucks];//creating Truck for generating new solution
			for(int i = 0; i<trucks;i++){
				gentruck[i]=new Truck(capacity);
			}
			temptobest(gentruck,mytrucks); //copying iteration solution to gentruck
			Random r = new Random();
			int counter=0;//counter for checking if selected 2 nodes are interchangeable 
			int randtr=0 , randnd=0, randnode1=0, randnode2=0; // randtr: random truck, randnd: random node from rantr, randnode1: # of the node randnd, randnode2: random node from mynodes
			while(counter==0){// selecting 2 nodes for 2-opt exchange
				 randtr = r.nextInt(mytrucks.length);
				 randnd = r.nextInt(mytrucks[randtr].route.size());
				 randnode1=mytrucks[randtr].route.get(randnd);
				 randnode2=r.nextInt(mynodes.length-1)+1;
				if(mytrucks[randtr].usedCap-mynodes[randnode1].demand+mynodes[randnode2].demand<= capacity && mytrucks[mynodes[randnode2].whichtruck].usedCap-mynodes[randnode2].demand+mynodes[randnode1].demand<= capacity )
					counter=1;// checks the capacity of the trucks if change of nodes is possible or not

			}//end of while 
			gentruck[randtr].route.add(randnd, randnode2);
			gentruck[randtr].route.remove(randnd+1);
			gentruck[randtr].Distance(mynodes);
			gentruck[mynodes[randnode2].whichtruck].route.add(mynodes[randnode2].state, randnode1);
			gentruck[mynodes[randnode2].whichtruck].route.remove(mynodes[randnode2].state+1);
			gentruck[mynodes[randnode2].whichtruck].Distance(mynodes);
			//arranges the places of selected nodes in trucks' route, changes node according to their stat in their respected trucks'
			double a =objectiveValue(gentruck);//calculates sum of all trucks distance *newly generated after 2-opt
			double b= objectiveValue(mytrucks);//calculates sum of all trucks distance *before generating a neighbor
			if(a<b){//if new generated value< old one
				stop[0]++; // increase accepted moves
				stop[1]=0; // succesive rejected moves resets
				//exchanged nodes' information gets updates
				mynodes[randnode1].whichtruck=mynodes[randnode2].whichtruck; //which truck nodes supplied from
				mynodes[randnode1].state=mynodes[randnode2].state;//what is state of the node in that truck
				mynodes[randnode2].whichtruck=randtr;
				mynodes[randnode2].state=randnd; 
				
				for(int i=0;i<gentruck.length;i++)//updating used capacity of the generated solution
					gentruck[i].calculateUsedCap(mynodes);
				temptobest(mytrucks,gentruck); //iteration solution updated to generated solution
				acceptMove(gentruck,bestsolution);//checks if generated solution better than best, if so updates best solution
			}
			else{
				int x =toss(gentruck,mytrucks,stop); // for worse values generated, selects them with some probability, returns 1 if not accepted or 0 if accepted
				if(x==1){
					stop[1]++; // increase the succesive rejected move size
					}
				else{
					stop[0]++; //increases accepted moves 
					stop[1]=0; // reset succesive rejected moves 
					mynodes[randnode1].whichtruck=mynodes[randnode2].whichtruck;
					mynodes[randnode1].state=mynodes[randnode2].state;
					mynodes[randnode2].whichtruck=randtr;
					mynodes[randnode2].state=randnd; 
					//updating nodes trucks and states
					stop[3]=(int) ((int)stop[3]*alpha/100); //decreasing the heat, because we used the bad move
				}
			}

		}//end of while
		
		//printing final best solution 
		System.out.printf("%n%nFinal found Solution:%n");
		for(int i=0;i<bestsolution.length;i++)
			bestsolution[i].printInfo(i+1);
		System.out.printf("Total Distance : %.3f %n# of Iterations : %d %n# of Accepted Moves : %d %n",objectiveValue(bestsolution),stop[2],stop[0]);

	}//end of void

	public static void initialSolution(Node[] node, Truck[] truck,Truck[] temp){
		ArrayList<Integer> nodes= new ArrayList<>();
		for(int i=1; i < node.length;i++)
			nodes.add(i);// creates the places of customers which scanned from txt file (assumed that 1 node is always depot)
		Random r = new Random();
		int counter=0;
		while(nodes.size()!=0){
			int x = r.nextInt(nodes.size());//randomly generated customer
			int y = r.nextInt(truck.length);//randomly selected truck
			int control = nodes.get(x);// index of customer

			if(counter>60){//if capacity is not enough for 60 succesive times clears the arrays and tries to generate new random solution
				for(int i=0; i<truck.length;i++){
					truck[i].usedCap=0;
					temp[i].usedCap=0;
					truck[i].route.clear();
					temp[i].route.clear();
				}//end for
				initialSolution(node,truck,temp);
				break;//if able found a solution in it breaks from loop and terminates function
			}//end if
			if(truck[y].Capacity-truck[y].usedCap<= node[control].demand){//because of selecting customers and trucks randomly
				counter++;												// some nodes are not able to be placed in trucks because of scattering unreasonably
				continue;												//increases the counter and tries to find another place
			}
			truck[y].route.add(nodes.get(x)); //add the customer to randomly generated trucks route *bestsolution
			temp[y].route.add(nodes.get(x));  //*iteration solution
			truck[y].addCap(node[control].demand); //used capacity increases
			temp[y].addCap(node[control].demand); 
			node[control].state=truck[y].route.size()-1; // giving node which index of route it represents in that truck
			node[control].whichtruck=y;					// giving node the # of the truck it will supply its demand
			nodes.remove(x); //removing randomly selected node from list to insure not repeating same nodes
		}//end of while loop

	}//end of initialSolution

	public static double objectiveValue(Truck[] truck){//sums up the distance trucks travels
		double result=0;
		for(int i=0;i<truck.length;i++)
			result=result+truck[i].totaldist;
		return result;
	}//end of objectiveValue
	
	public static void acceptMove(Truck[] gen, Truck[] best){// if generated solution better than best solutions
		if(objectiveValue(gen)<objectiveValue(best))
			temptobest(best,gen); //copy generated solution to best solution
	}
	
	public static int toss(Truck[]gen, Truck[] temp, int[] stop){

		double prob=1-(objectiveValue(gen)-objectiveValue(temp))/stop[3]; // creating probabilty with 1-deltaF/Heat
		if(Math.random()<prob){
			temptobest(temp,gen); //if probabilty meet, accepting bad move /*means we copy generated bad move to iteration solution and countiune
			return 0; //if move accepted
		}
		else
			return 1; //not accepted /*code updates stop[] values according to this returns
		
		
	}
	
	public static int stoppingConditions(int[]stop){
		int e=2, K=5; // e: % value, K: # of succesive reject move right
		if(stop[1]>=K && stop[0]<(int)e*stop[2]/100) //if more than K moves are rejected & # of accepted moves are less e% of total iterations
			return 0;//conditions met
		
		return 1; //keep generating
	}//end of stoppingConditions
	
	public static void temptobest(Truck[]best,Truck[]temp){//copy temp to best
		
		for(int i=0; i<best.length;i++){
			best[i].route.clear(); //clears the route of best 
			best[i].distance.clear(); //clears the distances of best
			for(int j=0;j<temp[i].route.size();j++){
				best[i].route.add(temp[i].route.get(j)); //then adds the temp route to best
				best[i].distance.add(temp[i].distance.get(j)); // same for distance
			}
			best[i].usedCap=temp[i].usedCap; //updates used capacity and total distance
			best[i].totaldist=temp[i].totaldist; // this is for preventing calculating this values from scracth again, helps shorting process
		}
		
	}//end of temptobest

}//end of main
