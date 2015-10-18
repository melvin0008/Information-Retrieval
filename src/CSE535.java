import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System;
import java.util.*;

class node{
	String docId;
	int freq;
	public node(String id,int f){
		docId=id;
		freq=f;
	}
	public int getfreq(){
		return freq;
	}
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof node)) {
	        return false;
	    }
	    node that = (node) other;
	    return this.docId.equals(that.docId)
	        && this.freq==that.freq;
	}
}

class postings{
	LinkedList<node> postingList;
	int count;
	public postings(LinkedList<node> pl,int c){
		postingList = pl;
		count=c;
	}
}

class InvertedIndex{
	Map<String, postings> invertedIndex1;
	Map<String, postings> invertedIndex2;
	
	public InvertedIndex(String filename) throws NumberFormatException, IOException{
		invertedIndex1 = new HashMap<String,postings>();
		invertedIndex2 = new HashMap<String,postings>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		try {
		    String line = null;
		    int vstart,pstart,pend,count;
			String postings,temp,vocab;
			String[] ary ;
		    while ((line = br.readLine()) != null) {
		    	ary=null;
		    	LinkedList<node> ll = new LinkedList<>();
		    	vstart=line.indexOf("\\c");
		        pstart = line.indexOf("\\m[");
		        vocab=line.substring(0,vstart);
		        count=Integer.parseInt(line.substring(vstart+2,pstart));
		        pend = line.indexOf("]",pstart);
		        postings = line.substring(pstart+3, pend);
		        ary=postings.split(",");
		        for(int i=0;i<ary.length;i++)
		        {
		        	temp=ary[i];
		        	ll.add(new node(temp.split("/")[0].trim(),Integer.parseInt(temp.split("/")[1].trim())));
		        }
		        @SuppressWarnings("unchecked")
				LinkedList<node> copyll = (LinkedList<node>) ll.clone();
		        Collections.sort(copyll, new Comparator<node>() {
		        	@Override
		            public int compare(final node object1, final node object2) {
		                return Integer.toString(object2.getfreq()).compareTo(Integer.toString(object1.getfreq()));
		        	}
		        });
		        
		        invertedIndex1.put(vocab,new postings(ll,count));
		        invertedIndex2.put(vocab,new postings(copyll,count));
		    }
		} finally {
		    br.close();
		}
	}
	public void printInvertedIndexes(){		
		for (String name: invertedIndex1.keySet()){
            LinkedList<node> value = invertedIndex1.get(name).postingList;  
            ListIterator<node> listIterator = value.listIterator();
            System.out.println(name);
            while(listIterator.hasNext()){
            	 System.out.print(listIterator.next().docId);
            }  
        } 
//		for (String name: invertedIndex2.keySet()){
//            LinkedList<node> value = invertedIndex2.get(name).postingList;  
//            ListIterator<node> listIterator = value.listIterator();
//            System.out.println(name);
//            while(listIterator.hasNext()){
//            	 System.out.print(listIterator.next().freq);
//            }  
//        } 
		
	}
	
	public String[] getTopKterms(int k){
			String[] topkterms = new String[k];
			PriorityQueue<node> maxh = new PriorityQueue<node>(k, new Comparator<node>() {
					@Override
					public int compare(node o1, node o2) {
						if(o1.getfreq() < o2.getfreq()) {
							return 1;
						} else if(o1.getfreq() > o2.getfreq()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
		
				for (String name: invertedIndex1.keySet()){
					int c = invertedIndex1.get(name).count;
					maxh.add(new node(name,c));
				}
				
				for(int i =0;i<k;i++){
					node n = maxh.poll();
					System.out.println(i+"->"+n.docId);
					topkterms[i] = n.docId;
				}
				return topkterms;	
	}
	
	private postings getPostings1(String term){
		return invertedIndex1.get(term);
	}
	
	private postings getPostings2(String term){
		return invertedIndex2.get(term);
	}
	
	private LinkedList<node> intersection(postings posting1,postings posting2){
		LinkedList<node> l1 = posting1.postingList;
		LinkedList<node> l2 = posting2.postingList;
		LinkedList<node> l3 = new LinkedList<node>();
		int c1 = posting1.count;
		int c2 = posting2.count;
		int i=0,j=0;
		while(i<c1){
			while(j<c2){
				if(Integer.parseInt(l1.get(i).docId)==Integer.parseInt(l2.get(j).docId)){
					l3.add(l1.get(i));
					break;
				}
				j++;
			}
			j=0;
			i++;
		}
		return l3;
	}
	
	@SuppressWarnings("unchecked")
	private LinkedList<node> union(postings posting1,postings posting2){
		LinkedList<node> l1 = posting1.postingList;
		LinkedList<node> l2 = posting2.postingList;
		LinkedList<node> l3 = new LinkedList<node>();
		int c1 = posting1.count;
		int c2 = posting2.count;
		int i=0,j=0,flag=0;
		
		l3=(LinkedList<node>) l1.clone();
		
		while(j<c2){
			while(i<c1){
				if(Integer.parseInt(l1.get(i).docId)==Integer.parseInt(l2.get(j).docId)){
					flag=1;
					break;
				}
				i++;
			}
			i=0;
			if(flag==0)
			{
				l3.add(l2.get(j));
			}
			flag=0;
			j++;
		}
		return l3;
	}
	
	private LinkedList<node> TAAThelper2(LinkedList<node> l,ArrayList<String> a){
		int size=l.size();
		if(a.size()<1){
			return l;
		}
		LinkedList<node> l1 = union(new postings(l,size),getPostings2(a.remove(0)));
		return TAAThelper2(l1,a);
	}
	
	private ArrayList<String> getArray(String... s){
		ArrayList<String> a= new ArrayList<String>();
		for(int i=0;i<s.length;i++){
			a.add(s[i]);
		}
		return a;
	}
	
	public LinkedList<node> TAATOr(String... s){
		ArrayList<String> a=getArray(s);
		return TAAThelper2(getPostings2(a.remove(0)).postingList,a);
	}
	
	private LinkedList<node> TAAThelper1(LinkedList<node> l,ArrayList<String> a){
		int size=l.size();
		if(a.size()<1){
			return l;
		}
		LinkedList<node> l1=intersection(new postings(l,size),getPostings2(a.remove(0)));
		return TAAThelper1(l1,a);
	}
	
	public LinkedList<node> TAATAnd(String... s){
		ArrayList<String> a=getArray(s);
		return TAAThelper1(getPostings2(a.remove(0)).postingList,a);
	}
	
	public LinkedList<node> TAATAndOptimized(String... s){
		ArrayList<String> a=getArray(s);
		ArrayList<String> aopt= new ArrayList<String>();
		LinkedList<node> n= new LinkedList<node>();
		for(int i=0;i<a.size();i++){
			n.add(new node(a.get(i),getPostings2(a.get(i)).count));
		}
		Collections.sort(n, new Comparator<node>() {
        	@Override
            public int compare(final node object1, final node object2) {
                return Integer.toString(object1.getfreq()).compareTo(Integer.toString(object2.getfreq()));
        	}
        });
		for(int i=0;i<n.size();i++){
			aopt.add(n.get(i).docId);
		}
		
		return TAAThelper1(getPostings2(aopt.remove(0)).postingList,aopt);
	} 
	
//	private Boolean checkfornull(LinkedList<node> helper){
//		for(int i=0;i<helper.size();i++){
//			if(helper.get(i).docId.isEmpty()){
//				return false;
//			}
//		}
//		return true;
//	}
	private Boolean checkequal(LinkedList<node> helper){
		node n=helper.get(0);
		for(int i=1;i<helper.size();i++){
			if(!helper.get(i).equals(n)){
				return false;
			}
		}
		return true;
	}
	
	private int findmin(LinkedList<node> helper){
		node n=helper.get(0);
		int min=Integer.parseInt(n.docId);
		int minpointer=0,temp;
		for(int i=1;i<helper.size();i++){
			temp=Integer.parseInt(helper.get(i).docId);
			if(temp<min){
				min=temp;
				minpointer=i;
			}
		}
		return minpointer;
	}
	
	private LinkedList<LinkedList<node>> createlol(ArrayList<String> a){
		LinkedList<LinkedList<node>> lol = new LinkedList<LinkedList<node>>();
		for(int i=0;i<a.size();i++){
			lol.add(getPostings1(a.get(i)).postingList);	
		}
		return lol;
	}
	
	public LinkedList<node> Daatintersection(String... s){
		ArrayList<String> a=getArray(s);
		int size = a.size();
		Integer[] pointerlist = new Integer[size];
		for(int i=0;i<pointerlist.length;i++){
			pointerlist[i]=0;
		}
		LinkedList<LinkedList<node>> lol = createlol(a);
		LinkedList<node> helper = new LinkedList<node>();
		LinkedList<node> result = new LinkedList<node>();
		for(int i=0;i<size;i++){
			helper.add(lol.get(i).get(pointerlist[i]));
		}
		
		while(true){
			if(checkequal(helper)){
				result.add(helper.get(0));
				for(int i=0;i<pointerlist.length;i++){
					pointerlist[i]++;
				}
			}
			else{
				int min=findmin(helper);
				pointerlist[min]++;
			}
			helper.clear();
			int f=0;
			for(int i=0;i<size;i++){
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
		return result;
	}
	public LinkedList<node> DaatUnion(String... s){
		ArrayList<String> a=getArray(s);
		int size = a.size();
		Integer[] pointerlist = new Integer[size];
		for(int i=0;i<pointerlist.length;i++){
			pointerlist[i]=0;
		}
		LinkedList<LinkedList<node>> lol = createlol(a);
		LinkedList<node> helper = new LinkedList<node>();
		LinkedList<node> result = new LinkedList<node>();
		for(int i=0;i<size;i++){
			helper.add(lol.get(i).get(pointerlist[i]));
		}
		
		while(true){
			if(checkequal(helper)){
				result.add(helper.get(0));
				for(int i=0;i<pointerlist.length;i++){
					pointerlist[i]++;
				}
			}
			else{
				int min=findmin(helper);
				result.add(lol.get(min).get(pointerlist[min]));
				pointerlist[min]++;
			}
			helper.clear();
			int f=1;
			for(int i=0;i<size;i++){
				if(lol.get(i).size()<=pointerlist[i]){
					f=1*f;
					continue;
				}
				f=f*0;
				helper.add(lol.get(i).get(pointerlist[i]));
			}
			if(f==1){
				break;
			}
			
		}
		return result;
	}
	
}

public class CSE535 {
	public static void main(String[] args) throws IOException {
		 InvertedIndex ii = new InvertedIndex("src/term1.idx");
		 LinkedList<node> l;
//		 String[] s= ii.getTopKterms(10);
//		 String term1,term2;
//		 term1="everyone";
//		 term2="in";
//		 postings posting1,posting2;
//		 posting1 = ii.getPostings2(term1);
//		 posting2 = ii.getPostings2(term2);
//		 l=ii.intersection(posting1, posting2);
		 l=ii.TAATAndOptimized("everyone");
//		 l=ii.TAATOr("everyone","in","as");
		 for(int i=0;i<l.size();i++){
			 System.out.println(l.get(i).docId);
		 }
		 System.out.println("----8645------");
		 l=ii.TAATAnd("everyone");
//		 l=ii.DaatUnion("everyone","in","as");
		 for(int i=0;i<l.size();i++){
			 System.out.println(l.get(i).docId);
		 }
//		 ii.printInvertedIndexes();
	}
}