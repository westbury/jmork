package mork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds all the dictionaries for a Mock document.
 * 
 * It also keeps track of the ids used by those dictionaries.  When new data is inserted
 * into the document, new ids may be required.  This class will provide those new ids,
 * guaranteed to be unique within the scope of the ids.
 *
 */
public class Dicts implements Iterable<Dict> {

    /** An immutable empty list of dictionaries */
    public static final Dicts EMPTY_LIST = new Dicts() {
    	public void addDictionary(Dict dict) {
    		throw new UnsupportedOperationException();
    	}
    };

	private final List<Dict> dicts = new ArrayList<>();
	
	private Map<ScopeTypes, TakenIds> takenForEachScope = new HashMap<>();
    {
    	takenForEachScope.put(ScopeTypes.ATOM_SCOPE, new TakenIds());
    	takenForEachScope.put(ScopeTypes.COLUMN_SCOPE, new TakenIds());
    }

	public Dicts() {
		// Empty list of dictionaries, but dictionaries may
		// be added later.
	}

	Dicts(List<Dict> dicts) {
		this.dicts.addAll(dicts);
		for (Dict dict : dicts) {
			addUsedReferences(dict);
		}
	}

	@Override
	public Iterator<Dict> iterator() {
		return dicts.iterator();
	}

	public boolean isEmpty() {
		return dicts.isEmpty();
	}
	
    /**
     * 
     * @param scope
     * @return refid (without the preceding ^)
     */
	public String getNextReference(ScopeTypes scope) {
		TakenIds taken = takenForEachScope.get(scope);
		return taken.getNextReference();
	}

	public void addDictionary(Dict dict) {
		dicts.add(dict);
		addUsedReferences(dict);
	}

	private void addUsedReferences(Dict dict) {
		ScopeTypes scope = dict.getDefaultScope();
		TakenIds usedReferencesForScope = takenForEachScope.get(scope);
		for (String id : dict.getAliases().getKeySet()) {
			usedReferencesForScope.registerAsUsed(id);
		}
	}

	public List<Dict> getDictionaries() {
		return dicts;
	}

}
