package greenlab.handlers;

import static greenlab.utils.GreenlabConstants.ARRAYLIST_TYPE;
import static greenlab.utils.GreenlabConstants.ATRIBUTELIST_TYPE;
import static greenlab.utils.GreenlabConstants.CONCURRENTMAP_TYPE;
import static greenlab.utils.GreenlabConstants.CONCURRENTSET_TYPE;
import static greenlab.utils.GreenlabConstants.CONCURRENTSKIPLIST_TYPE;
import static greenlab.utils.GreenlabConstants.COPYONWRITELIST_TYPE;
import static greenlab.utils.GreenlabConstants.FILE_LIST_1M;
import static greenlab.utils.GreenlabConstants.FILE_LIST_250K;
import static greenlab.utils.GreenlabConstants.FILE_LIST_25K;
import static greenlab.utils.GreenlabConstants.FILE_MAP_1M;
import static greenlab.utils.GreenlabConstants.FILE_MAP_250K;
import static greenlab.utils.GreenlabConstants.FILE_MAP_25K;
import static greenlab.utils.GreenlabConstants.FILE_SET_1M;
import static greenlab.utils.GreenlabConstants.FILE_SET_250K;
import static greenlab.utils.GreenlabConstants.FILE_SET_25K;
import static greenlab.utils.GreenlabConstants.HASHLINKED_TYPE;
import static greenlab.utils.GreenlabConstants.HASHMAP_TYPE;
import static greenlab.utils.GreenlabConstants.HASHSET_TYPE;
import static greenlab.utils.GreenlabConstants.HASHTABLE_TYPE;
import static greenlab.utils.GreenlabConstants.LINKEDHASH_TYPE;
import static greenlab.utils.GreenlabConstants.LINKEDLIST_TYPE;
import static greenlab.utils.GreenlabConstants.LIST;
import static greenlab.utils.GreenlabConstants.MAP;
import static greenlab.utils.GreenlabConstants.PROPERTIES_TYPE;
import static greenlab.utils.GreenlabConstants.ROLELIST_TYPE;
import static greenlab.utils.GreenlabConstants.ROLEUNRESOLVEDLIST_TYPE;
import static greenlab.utils.GreenlabConstants.SET;
import static greenlab.utils.GreenlabConstants.SIMPLEBINDING_TYPE;
import static greenlab.utils.GreenlabConstants.SIZE_1M;
import static greenlab.utils.GreenlabConstants.SIZE_250K;
import static greenlab.utils.GreenlabConstants.SIZE_25K;
import static greenlab.utils.GreenlabConstants.STACK_TYPE;
import static greenlab.utils.GreenlabConstants.TREEMAP;
import static greenlab.utils.GreenlabConstants.TREESET_TYPE;
import static greenlab.utils.GreenlabConstants.UIDEFAULTS_TYPE;
import static greenlab.utils.GreenlabConstants.VECTOR_TYPE;
import static greenlab.utils.GreenlabConstants.WEAKHASH_TYPE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import greenlab.models.Invocation;
import greenlab.models.InvocationCost;
import greenlab.models.Variable;

public class GreenlabEnergyManager {

	private String analyseSize = "";
	private Map<String,List<InvocationCost>> listresults;
	private Map<String,List<InvocationCost>> setresults;
	private Map<String,List<InvocationCost>> mapresults;

	public GreenlabEnergyManager(String size) {
		this.analyseSize = size;
		this.listresults = new HashMap<String,List<InvocationCost>>();
		this.setresults = new HashMap<String,List<InvocationCost>>();
		this.mapresults = new HashMap<String,List<InvocationCost>>();
		this.readCollectionsResults();
		this.normaliseCosts();
	}
	
	private void readCollectionsResults() {
		if(this.analyseSize.equals(SIZE_25K)) {
			this.readFile(FILE_LIST_25K,LIST);
			this.readFile(FILE_SET_25K,SET);
			this.readFile(FILE_MAP_25K,MAP);
		}else if(this.analyseSize.equals(SIZE_250K)) {
			this.readFile(FILE_LIST_250K,LIST);
			this.readFile(FILE_SET_250K,SET);
			this.readFile(FILE_MAP_250K,MAP);
		}else if(this.analyseSize.equals(SIZE_1M)) {
			this.readFile(FILE_LIST_1M,LIST);
			this.readFile(FILE_SET_1M,SET);
			this.readFile(FILE_MAP_1M,MAP);
		}
	}

	private void readFile(String file, String collectionType) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new InputStreamReader(loader.getResourceAsStream(file)));
			int isHeader = 0;
			while ((line = br.readLine()) != null) {
				if(isHeader > 0) {
					this.parseData(collectionType, line.split(cvsSplitBy));
				}
				isHeader++;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseData(String collectionType, String[] data) {
		String type = data[0];
		String method = data[1];
		float joules = Float.valueOf(data[2]);
		float ms = Float.valueOf(data[3]);
//		float mb = Float.valueOf(data[4]);
		float mb = 0;

		if(collectionType.equals(LIST)) {
			if(!this.listresults.containsKey(method)) {
				this.listresults.put(method, new ArrayList<InvocationCost>());
			}
			this.listresults.get(method).add(new InvocationCost(type, method, joules, ms, mb));
		}else if(collectionType.equals(SET)) {
			if(!this.setresults.containsKey(method)) {
				this.setresults.put(method, new ArrayList<InvocationCost>());
			}
			this.setresults.get(method).add(new InvocationCost(type, method, joules, ms, mb));
		}else if(collectionType.equals(MAP)) {
			if(!this.mapresults.containsKey(method)) {
				this.mapresults.put(method, new ArrayList<InvocationCost>());
			}
			this.mapresults.get(method).add(new InvocationCost(type, method, joules, ms, mb));
		}
	}

	private void normaliseCosts() {
		for (String k : listresults.keySet()) {
			List<InvocationCost> l = listresults.get(k);
			l.sort((i1, i2) -> i1.getMs() <= i2.getMs() ? -1 : 1);
			float minMs = l.get(0).getMs();
			l.sort((i1, i2) -> i1.getJoules() <= i2.getJoules() ? -1 : 1);
			float minJoules = l.get(0).getJoules();
			l.sort((i1, i2) -> i1.getMb() <= i2.getMb() ? -1 : 1);
			float minMb = l.get(0).getMb();
			
			for (InvocationCost i : l) {
				i.setNormalJoules(i.getJoules() / minJoules);
				i.setNormalMs(i.getMs() / minMs);
				i.setNormalMb(i.getMb() / minMb);
			}
		}
		
		for (String k : setresults.keySet()) {
			List<InvocationCost> l = setresults.get(k);
			l.sort((i1, i2) -> i1.getMs() <= i2.getMs() ? -1 : 1);
			float minMs = l.get(0).getMs();
			l.sort((i1, i2) -> i1.getJoules() <= i2.getJoules() ? -1 : 1);
			float minJoules = l.get(0).getJoules();
			l.sort((i1, i2) -> i1.getMb() <= i2.getMb() ? -1 : 1);
			float minMb = l.get(0).getMb();
			
			for (InvocationCost i : l) {
				i.setNormalJoules(i.getJoules() / minJoules);
				i.setNormalMs(i.getMs() / minMs);
				i.setNormalMs(i.getMs() / minMs);
				i.setNormalMb(i.getMb() / minMb);
			}
		}
		
		for (String k : mapresults.keySet()) {
			List<InvocationCost> l = mapresults.get(k);
			l.sort((i1, i2) -> i1.getMs() <= i2.getMs() ? -1 : 1);
			float minMs = l.get(0).getMs();
			l.sort((i1, i2) -> i1.getJoules() <= i2.getJoules() ? -1 : 1);
			float minJoules = l.get(0).getJoules();
			l.sort((i1, i2) -> i1.getMb() <= i2.getMb() ? -1 : 1);
			float minMb = l.get(0).getMb();
			
			for (InvocationCost i : l) {
				i.setNormalJoules(i.getJoules() / minJoules);
				i.setNormalMs(i.getMs() / minMs);
				i.setNormalMs(i.getMs() / minMs);
				i.setNormalMb(i.getMb() / minMb);
			}
		}
	}
	
	public Map<String,List<InvocationCost>> normaliseVariableCost(Variable v) {
		String type = this.getTypes(v)[0];
		String file_type = this.getTypes(v)[1];
		Map<String,List<InvocationCost>> result = new Hashtable<String, List<InvocationCost>>();
		
		if(type != null && file_type != null) {
			Map<String,Invocation> variableInvocations = v.getInvocations();
			Map<String,List<InvocationCost>> map = null;
			
			if(file_type.equals(LIST)) {
				map = this.listresults;
			}else if(file_type.equals(SET)) {
				map = this.setresults;
			}else if(file_type.equals(MAP)) {
				map = this.mapresults;
			}
			
			System.out.println(">> " + v.getType() + " " + v.getName());
			InvocationCost newivc = null;
			for(Invocation iv : variableInvocations.values()) {
				if(map.containsKey(iv.getName())) {
					int totalInvocations = iv.getTotalInvocations();
					for(InvocationCost ivc : map.get(iv.getName())) {
						if(!result.containsKey(ivc.getType())) {
							result.put(ivc.getType(), new ArrayList<InvocationCost>());
						}
						newivc = new InvocationCost(ivc.getType(), iv.getName(), totalInvocations * ivc.getJoules(), totalInvocations * ivc.getMs(), totalInvocations * ivc.getMb(), ivc.getType().equals(type));
						newivc.setNormalJoules(totalInvocations * ivc.getNormalJoules());
						newivc.setNormalMs(totalInvocations * ivc.getNormalMs());
						newivc.setNormalMb(totalInvocations * ivc.getNormalMb());
						result.get(ivc.getType()).add(newivc);
						if(ivc.getType().equals(type)) {
							System.out.println("  >> method " + iv.getName() + " ("+totalInvocations+") " + "has a cost of " + newivc.getJoules() + "J" + " (" +newivc.getNormalJoules() + ") and " + newivc.getMs() + "ms" +" ("+newivc.getNormalMs() +") and " + newivc.getMb() + "mb " +" ("+newivc.getNormalMb() +")");
						}
					}
				}
			}
			System.out.println("");
		}
		return result;
	}
	
	private String[] getTypes(Variable v) {
		String type = null;
		String file_type = null;

		if(v.getType().contains(ARRAYLIST_TYPE)) {
			type = ARRAYLIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(ATRIBUTELIST_TYPE)) {
			type = ATRIBUTELIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(COPYONWRITELIST_TYPE)) {
			type = COPYONWRITELIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(LINKEDLIST_TYPE)) {
			type = LINKEDLIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(ROLELIST_TYPE)) {
			type = ROLELIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(ROLEUNRESOLVEDLIST_TYPE)) {
			type = ROLEUNRESOLVEDLIST_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(STACK_TYPE)) {
			type = STACK_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(VECTOR_TYPE)) {
			type = VECTOR_TYPE;
			file_type = LIST;
		}else if(v.getType().contains(CONCURRENTSET_TYPE)) {
			type = CONCURRENTSET_TYPE;
			file_type = SET;
		}else if(v.getType().contains(HASHSET_TYPE)) {
			type = HASHSET_TYPE;
			file_type = SET;
		}else if(v.getType().contains(LINKEDHASH_TYPE)) {
			type = LINKEDHASH_TYPE;
			file_type = SET;
		}else if(v.getType().contains(TREESET_TYPE)) {
			type = TREESET_TYPE;
			file_type = SET;
		}else if(v.getType().contains(CONCURRENTMAP_TYPE)) {
			type = CONCURRENTMAP_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(CONCURRENTSKIPLIST_TYPE)) {
			type = CONCURRENTSKIPLIST_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(HASHMAP_TYPE)) {
			type = HASHMAP_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(HASHTABLE_TYPE)) {
			type = HASHTABLE_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(HASHLINKED_TYPE)) {
			type = HASHLINKED_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(PROPERTIES_TYPE)) {
			type = PROPERTIES_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(SIMPLEBINDING_TYPE)) {
			type = SIMPLEBINDING_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(TREEMAP)) {
			type = TREEMAP;
			file_type = MAP;
		}else if(v.getType().contains(UIDEFAULTS_TYPE)) {
			type = UIDEFAULTS_TYPE;
			file_type = MAP;
		}else if(v.getType().contains(WEAKHASH_TYPE)) {
			type = WEAKHASH_TYPE;
			file_type = MAP;
		}

		return new String[] {type,file_type};
	}

}
