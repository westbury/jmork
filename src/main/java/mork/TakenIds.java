package mork;

import java.util.HashSet;
import java.util.Set;

/**
 * This class keeps track of reference ids already used for a scope. It provides
 * available (unused) reference ids on request.
 * <P>
 * Note that this implementation does fill up gaps, but only if the gap existed
 * in the Mork document as loaded. If a gap is created as a result of deletions
 * from the document after this set of used reference ids was created then those
 * gaps won't be re-used.
 */
public class TakenIds {

	private Set<Integer> taken = new HashSet<Integer>();
	
	private int nextPotentiallyAvailable = 128;

    /**
     * 
     * @return refid (without the preceding ^)
     */
	public String getNextReference() {
		do {
			int proposedRefNumber = nextPotentiallyAvailable++;
			if (!taken.contains(proposedRefNumber)) {
				String refId = Integer.toHexString(proposedRefNumber);
				return refId;
			}
		} while (true);
	}

	public void registerAsUsed(String refId) {
		int idAsNumber = Integer.parseInt(refId, 16);
		taken.add(idAsNumber);
		
	}

}
