import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System;
import java.util.*;

class Node{
	String docId;
	int freq;
	public Node(String id,int f){
		docId=id;
		freq=f;
	}
	public int getfreq(){
		return freq;
	}
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof Node)) {
	        return false;
	    }
	    Node that = (Node) other;
	    return this.docId.compareTo(that.docId)==0;
	   
	}
}

class Postings{
	LinkedList<Node> postingList;
	int count;
	public Postings(LinkedList<Node> pl,int c){
		postingList = pl;
		count=c;
	}
}

class InvertedIndex{
	Map<String, Postings> invertedIndex1;
	Map<String, Postings> invertedIndex2;
	String logfile;
	//Create Inverted Index
	public InvertedIndex(String filename,String log) throws NumberFormatException, IOException{
		invertedIndex1 = new HashMap<String,Postings>();
		invertedIndex2 = new HashMap<String,Postings>();
		logfile = log;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		try {
		    String line = null;
		    int vstart,pstart,pend,count;
			String Postings,temp,vocab;
			String[] ary ;
		    while ((line = br.readLine()) != null) {
		    	ary=null;
		    	LinkedList<Node> ll = new LinkedList<>();
		    	vstart=line.indexOf("\\c");
		        pstart = line.indexOf("\\m[");
		        vocab=line.substring(0,vstart);
		        count=Integer.parseInt(line.substring(vstart+2,pstart));
		        pend = line.indexOf("]",pstart);
		        Postings = line.substring(pstart+3, pend);
		        ary=Postings.split(",");
		        for(int i=0;i<ary.length;i++)
		        {
		        	temp=ary[i];
		        	ll.add(new Node(temp.split("/")[0].trim(),Integer.parseInt(temp.split("/")[1].trim())));
		        }
		        @SuppressWarnings("unchecked")
				LinkedList<Node> copyll = (LinkedList<Node>) ll.clone();
		        //Sort for InvertedIndex2
		        Collections.sort(copyll, new Comparator<Node>() {
		        	@Override
		            public int compare(final Node object1, final Node object2) {
		                return Integer.toString(object2.getfreq()).compareTo(Integer.toString(object1.getfreq()));
		        	}
		        });
		        
		        invertedIndex1.put(vocab,new Postings(ll,count));
		        invertedIndex2.put(vocab,new Postings(copyll,count));
		    }
		} finally {
		    br.close();
		}
	}
	
	//Get TopKterms using priorityqueue heap
	public void getTopKterms(int k){
			String[] topkterms = new String[k];
			String s = "";
			PriorityQueue<Node> maxh = new PriorityQueue<Node>(k, new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						if(o1.getfreq() < o2.getfreq()) {
							return 1;
						} else if(o1.getfreq() > o2.getfreq()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
		
				//Add elements to the heap
				for (String name: invertedIndex1.keySet()){
					int c = invertedIndex1.get(name).count;
					maxh.add(new Node(name,c));
				}
				
				//Get Top K terms
				for(int i =0;i<k;i++){
					Node n = maxh.poll();
					if(i!=k-1){
					s=s+n.docId+", ";
					}
					else{
						s=s+n.docId;
					}
					topkterms[i] = n.docId;
				}
				
				PrintLog("FUNCTION: getTopK "+k);
				PrintLog("Result: "+s);	
	}
	
	//Function for getPostings from invertedindex1
	private Postings getPostings1(String term){
		return invertedIndex1.get(term);
	}
	//Function for getPostings from invertedindex2
	private Postings getPostings2(String term){
		return invertedIndex2.get(term);
	}
	
	//Print Function for printing LinkedList
	private void PrintLinkedList(LinkedList<Node> l,String result){
		int k;
		String s="";
		k=l.size();
		for(int i=0;i<k;i++){
			 if(i!=k-1){
				 s=s+l.get(i).docId+", ";
			 }
			 else{
				 s=s+l.get(i).docId;
			 }
			
		 }
		PrintLog(result+s);
	}
	
	//Get postings from the Inverted Index hashmap
	public void getPostings(String term){
		LinkedList<Node> l1,l2;
		Postings p1,p2;
		p1=getPostings1(term);
		p2=getPostings2(term);
		PrintLog("FUNCTION: getPostings "+term);
		if(p1!=null && p2!=null)
		{
			l1 = p1.postingList;
			l2 = p2.postingList;
			PrintLinkedList(l1,"Ordered by doc IDs: ");
			PrintLinkedList(l2,"Ordered by doc TF: ");
		}
		else{
			PrintLog("Term not found");
		}
	}
	
	//Intersection code
	private Postings intersection(Postings posting1,Postings posting2){
		LinkedList<Node> l1 = posting1.postingList;
		LinkedList<Node> l2 = posting2.postingList;
		LinkedList<Node> l3 = new LinkedList<Node>();
		int c1 = posting1.count;
		int c2 = posting2.count;
		int i=0,j=0,counter=0;
		while(i<c1){
			while(j<c2){
				//Compare each term and add if equal
				if(Integer.parseInt(l1.get(i).docId)==Integer.parseInt(l2.get(j).docId)){
					counter+=1;
					l3.add(l1.get(i));
					break;
				}
				j++;
			}
			counter+=j;
			j=0;
			i++;
		}
		return new Postings(l3,counter);
	}
	
	//Function for union of two lists
	@SuppressWarnings("unchecked")
	private Postings union(Postings posting1,Postings posting2){
		LinkedList<Node> l1 = posting1.postingList;
		LinkedList<Node> l2 = posting2.postingList;
		LinkedList<Node> l3 = new LinkedList<Node>();
		int c1 = posting1.count;
		int c2 = posting2.count;
		int i=0,j=0,flag=0,counter=0;
		
		//Clone the first list
		l3=(LinkedList<Node>) l1.clone();
		
		while(j<c2){
			while(i<c1){
				//Compare if element is present in List1
				if(Integer.parseInt(l1.get(i).docId)==Integer.parseInt(l2.get(j).docId)){
					counter+=1;
					flag=1; //Raise flag
					break;
				}
				i++;
			}
			counter+=i;
			i=0;
			//If element not present insert
			if(flag==0)
			{
				l3.add(l2.get(j));
			}
			flag=0;
			j++;
		}
		return new Postings(l3,counter);
	}
	
	//Function which recurses and returns the list of union DocIds for ABD
	private Postings TAAThelper1(LinkedList<Node> l,ArrayList<String> a,int count){
		int size=l.size();
		//End of recursion with return as the final list and number of comparisons
		if(a.size()<1){
			 return new Postings(l,count);
		}
		Postings q=getPostings2(a.remove(0));
		if(q==null){
			return null; 
		}
		Postings p= intersection(new Postings(l,size),q);  //union function called to get the union of the two lists
		LinkedList<Node> l1=p.postingList;
		count+=p.count;  //Counter for comparison
		return TAAThelper1(l1,a,count);
	}
	
	//Function which recurses and returns the list of union DocIds for OR
	private Postings TAAThelper2(LinkedList<Node> l,ArrayList<String> a,int count){
		int size=l.size();
		//End of recursion with return as the final list and number of comparisons
		if(a.size()<1)
		{
			return new Postings(l,count);
		}
		Postings q=getPostings2(a.remove(0));
		while(q==null){
			if(a.size()==0){
				return null;
			}
			q=getPostings2(a.remove(0));
		}
		Postings p = union(new Postings(l,size),q); //union function called to get the union of the two lists
		LinkedList<Node> l1 = p.postingList; 
		count += p.count; //Counter for comparison
		return TAAThelper2(l1,a,count);
	}
	//Converts String[] to ArrayList
	private ArrayList<String> getArray(String[] s){
		ArrayList<String> a= new ArrayList<String>();
		for(int i=0;i<s.length;i++){
			a.add(s[i]);
		}
		return a;
	}
	
	//Function which recurses and returns the list of intersected DocIds
	private Postings TAATOrhelper(String[] s){
		ArrayList<String> b=getArray(s);
		ArrayList<String> a=cleanarray(b);
		if(a.size()==0){
			return null;
		}
		Postings p=getPostings2(a.remove(0));
		return TAAThelper2(p.postingList,a,0);

	}
	//Function required to initiate the recursion for optimized And
	private Postings TAATAndhelper(String[] s){
		ArrayList<String> a=getArray(s);
		Postings p=getPostings2(a.remove(0));
		if(p!=null){
			return TAAThelper1(p.postingList,a,0);
		}
		else{
			return null; //Returns null if even one list is not found
		}
		
	}

	//Code written to reorder array for optimization
	private ArrayList<String> ReorderArray(String[] s){
		ArrayList<String> a=getArray(s);
		Postings p;
		ArrayList<String> aopt= new ArrayList<String>();
		LinkedList<Node> n= new LinkedList<Node>();
		for(int i=0;i<a.size();i++){
			p=getPostings2(a.get(i));
			if(p!=null){
				n.add(new Node(a.get(i),p.count));	
			}
		}
		Collections.sort(n, new Comparator<Node>() {
        	@Override
            public int compare(final Node object1, final Node object2) {
                return Integer.toString(object1.getfreq()).compareTo(Integer.toString(object2.getfreq()));
        	}
        });
	
		
		for(int i=0;i<n.size();i++){
			aopt.add(n.get(i).docId);
		}
		return aopt;
	}
	
	//Function required to initiate the recursion for optimized And
	private Postings TAATAndOptimizedhelper(String[] s){
		ArrayList<String> aopt=ReorderArray(s);
		if(aopt.size()==0){
			return null;
		}
		Postings p=getPostings2(aopt.remove(0));
		if(p==null)
			return null; 
		return TAAThelper1(p.postingList,aopt,0);
	} 
	
	//Function required to initiate the recursion for optimized Or
	private Postings TAATOrOptimizedhelper(String[] s){
		 ArrayList<String> aopt=ReorderArray(s);
		 if(aopt.size()==0){
				return null;
		}
		Postings p=getPostings2(aopt.remove(0));
		while(p==null){
			if(aopt.size()==0){
				return null;
			}
			p=getPostings2(aopt.remove(0));
		} 
		return TAAThelper2(p.postingList,aopt,0);
	}
	
	//Print logic for TAAT
	private void PrintTAAT(Postings p1, Postings p2,String[] s,String query){
		long startTime = System.nanoTime();
		String k="";
		
		for(int i =0;i<s.length;i++){
			if(i<s.length-1){
				k+=s[i]+",";	
			}
			else{
				k+=s[i];
			}
		}
		PrintLog("FUNCTION: termAtATimeQuery"+query+" "+k);
		if(p1==null||p2==null){
			PrintLog("Terms not found");
			return;
		}
			
		LinkedList<Node> l1;
		int comparisons1,comparisons2;
		comparisons1=p1.count;
		comparisons2=p2.count;
		l1=p1.postingList;
		//Sort to get the output in increasing order of docId
		Collections.sort(l1, new Comparator<Node>() {
        	@Override
            public int compare(final Node object1, final Node object2) {
                return (object1.docId).compareTo(object2.docId);
        	}
        });
		PrintLog(Integer.toString(l1.size())+" documents are found");
		PrintLog(Integer.toString(comparisons1)+" comparisons are made");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		double seconds = (double)duration / 1000000000.0;
		PrintLog(Double.toString(seconds)+ "seconds are used");
		PrintLog(Integer.toString(comparisons2)+" comparisons are made with optimization");
		PrintLinkedList(l1,"Result: ");
	}
	
	//Print logic for DAAT
	private void PrintDAAT(Postings p1,String[] s,String query){
		long startTime = System.nanoTime();
		String k="";
		
		for(int i =0;i<s.length;i++){
			if(i<s.length-1){
				k+=s[i]+",";	
			}
			else{
				k+=s[i];
			}
		}
		PrintLog("FUNCTION: DocAtATimeQuery"+query+" "+k);
		if(p1==null){
			PrintLog("Terms not found");
			return;
		}
			
		LinkedList<Node> l1;
		int comparisons1;
		comparisons1=p1.count;
		l1=p1.postingList;
		PrintLog(Integer.toString(l1.size())+" documents are found");
		PrintLog(Integer.toString(comparisons1)+" comparisons are made");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		double seconds = (double)duration / 1000000000.0; //get time
		PrintLog(Double.toString(seconds)+ "seconds are used");
		PrintLinkedList(l1,"Result: ");
		
	}
	//main function for TAATAnd
	public void termAtATimeQueryAnd(String[] s){
		Postings p1,p2;
		p1=TAATAndhelper(s); //gets the intersected set of the docs and also comparison counter
		p2=TAATAndOptimizedhelper(s); //optimized value for the above
		PrintTAAT(p1,p2,s,"And"); //Log the files
	}
	
	//main function for TAATOR
	public void termAtATimeQueryOr(String[] s){
		Postings p1,p2;
		p1=TAATOrhelper(s); //gets the intersected set of the docs and also comparison counter
		p2=TAATOrOptimizedhelper(s); //optimized value for the above
		PrintTAAT(p1,p2,s,"Or");  //Log the files
	}
	
	private LinkedList<LinkedList<Node>> createlol(ArrayList<String> a,String query){
		LinkedList<LinkedList<Node>> lol = new LinkedList<LinkedList<Node>>();
		Postings p;
		for(int i=0;i<a.size();i++){
			p=getPostings1(a.get(i));
			if(p!=null){
				lol.add(p.postingList);
			}
			else{
				if(query.compareTo("And")==0){
					return null;
				}
			}		
		}
		return lol;
	}
	
	public void daatIntersection(String[] s){
		ArrayList<String> a=getArray(s);
		int size = a.size();
		int counter=0,i;
		Node n;
		Boolean checkequal;
		Integer[] pointerlist = new Integer[size]; //Creating a pointerlist to keep a track of pointers for each term  
		for(i=0;i<pointerlist.length;i++){
			pointerlist[i]=0;
		}
		
		LinkedList<LinkedList<Node>> lol = createlol(a,"And");    //Cleanarray removes null terms
		if(lol!=null){
			LinkedList<Node> helper = new LinkedList<Node>(); 	  //helper list will keep a track of all the current elements at which the pointers are.
			LinkedList<Node> result = new LinkedList<Node>();	
			for(i=0;i<size;i++){
				helper.add(lol.get(i).get(pointerlist[i])); 	  //Populating the helper list
			}
			while(true){
				//Checkequal written to check wheter all the elements of the helper list are equal
				checkequal=true;
				n=helper.get(0);
				for(i=1;i<helper.size();i++){
					
					if(!helper.get(i).equals(n)){
						checkequal=false;
					}
				}
				counter+=i;
				if(checkequal){
					//If equal add one copy and increment all pointers
					result.add(helper.get(0));
					for(i=0;i<pointerlist.length;i++){
						pointerlist[i]++;
					}
				}
				else{
					//Find minimum
					n=helper.get(0);
					int min=Integer.parseInt(n.docId);
					int minpointer=0,temp;
					for(i=1;i<helper.size();i++){
						temp=Integer.parseInt(helper.get(i).docId);
						if(temp<min){
							min=temp;
							minpointer=i;
						}
					}
					//Add element and increment pointer for minimum one
					counter+=i+1;
					pointerlist[minpointer]++;
				}
				helper.clear();
				int f=0;
				//code written to break out of the loop
				for(i=0;i<size;i++){
					if(lol.get(i).size()<=pointerlist[i]){
						f=1;
						break;
					}
					helper.add(lol.get(i).get(pointerlist[i]));
				}
				if(f==1){
					break;
				}
				
			}
			PrintDAAT(new Postings(result,counter),s,"And");
		}
		else{
			PrintLog("Terms not found");
		}
		
	}
	
	//Function written to remove nulls in the array
	@SuppressWarnings("null")
	private ArrayList<String> cleanarray(ArrayList<String> badarray){
		Postings p;
		String s;
		ArrayList<String> cleant= new ArrayList<String>();
		for(int i=0;i<badarray.size();i++){
			s=badarray.get(i);
			p=getPostings1(badarray.get(i));
			if(p!=null){
				cleant.add(s);
			}
		}
		return cleant;
	}
	
	//Function for DAATUnion
	public void daatUnion(String[] s){
		ArrayList<String> b=getArray(s);
		int i,j,counter=0;
		Boolean checkequal;
		Node n,m;
		ArrayList<String> a=cleanarray(b);    //Cleanarray removes null terms
		if(a.size()==0)
		{
			PrintLog("Terms not found");
			return;
		}
		int size = a.size();
		Integer[] pointerlist = new Integer[size]; //Creating a pointerlist to keep a track of pointers for each term  
		for(i=0;i<pointerlist.length;i++){
			pointerlist[i]=0;
		}
		
		LinkedList<LinkedList<Node>> lol = createlol(a,"Or");  //Creating a ListOfList
		
		LinkedList<Node> helper = new LinkedList<Node>(); //helper list will keep a track of all the current elements at which the pointers are.
		LinkedList<Node> result = new LinkedList<Node>();//used for result
		for(i=0;i<size;i++){
			helper.add(lol.get(i).get(pointerlist[i]));  //Populating the helper list
		}
		
		while(true){ 
			//Checkequal written to check wheter all the elements of the helper list are equal 
			checkequal=true;
			n=helper.get(0);
			for(i=1;i<helper.size();i++){
				if(helper.get(i)==null || n==null){
					checkequal=false;
					break;
				}		
				if(!helper.get(i).equals(n)){
					checkequal=false;
					break;
				}
			}
			counter+=i;
			if(checkequal){
				//If equal add one copy and increment all pointers
				result.add(helper.get(0));
				for(i=0;i<pointerlist.length;i++){
					pointerlist[i]++;
				}
			}
			else{
				//Find minimum
				int min=99999;
				int minpointer=0,temp;
				for(i=0;i<helper.size();i++){
					n=helper.get(i);
					if(n!=null){
						temp=Integer.parseInt(n.docId);
						if(temp<min){
							min=temp;
							minpointer=i;
						}
					}
				}
				if(minpointer==99999){
					break;
				}
				counter+=i+1;
				//Increment pointers for all elements equal to the minimum
				for(j=minpointer+1;j<helper.size();j++){
					m=helper.get(j);
					if(m!=null){
						counter+=1;
						if(Integer.parseInt(m.docId)==min){
							pointerlist[j]++;
						}
					}
				}
				//Add element and increment pointer for minimum one
				LinkedList<Node> n2=lol.get(minpointer);
				int l=pointerlist[minpointer];
				result.add(n2.get(l));
				pointerlist[minpointer]++;
			}
			helper.clear();
			int f=1;
			//code written to break out of the loop
			for(i=0;i<size;i++){
				if(lol.get(i).size()<=pointerlist[i]){
					f=1*f;
					//Adding null to maintain list position
					helper.add(null);
					continue;
				}
				f=f*0;
				helper.add(lol.get(i).get(pointerlist[i]));
			}
			if(f==1){
				break;
			}
			
		}
		PrintDAAT(new Postings(result,counter),s,"Or");

	}
	
	//Function to print Log file
	private void PrintLog(String s){
		try
		{
		    String filename= logfile;
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(s+"\n");//appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
}

public class CSE535Assignment {
	public static void main(String[] args) throws IOException {
		String termfile,outputfile,inputfile,line=null;
		int topk;
		termfile=args[0];
		outputfile=args[1];
		topk=Integer.parseInt(args[2]);
		inputfile=args[3];
		String[] s;
		
		BufferedReader br = new BufferedReader(new FileReader(inputfile));
		InvertedIndex ii = new InvertedIndex(termfile,outputfile);
		ii.getTopKterms(topk);
		while((line = br.readLine()) != null){        //Read line by line
			 s=line.split(" ");
			 for(int i=0;i<s.length;i++){
				 ii.getPostings(s[i]);
			 }
			 ii.termAtATimeQueryAnd(s);
			 ii.termAtATimeQueryOr(s);
			 ii.daatIntersection(s);
			 ii.daatUnion(s);
		}
		br.close();
	}
}