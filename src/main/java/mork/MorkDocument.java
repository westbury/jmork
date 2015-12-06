package mork;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A Mork Document represents a Mork database and provides structured access to
 * the whole document at once (in contrast to the Mork Parser, which is
 * event-based)
 * 
 * @author mhaller
 */
public class MorkDocument implements EventListener {

    /** Internal container for Dictionaries */
    private Dicts dicts = new Dicts(new LinkedList<Dict>());

    /** Internal container for Rows */
    private List<Row> rows = new LinkedList<Row>();

    /** Internal container for Tables */
    private List<Table> tables = new LinkedList<Table>();

    /**
     * Creata a new Mork Document using the given content
     * 
     * @param reader
     *            the Mork content
     */
    public MorkDocument(Reader reader) {
       this(reader,new DefaultExceptionHandler());
    }

    public MorkDocument(Reader reader,
			ExceptionHandler exceptionHandler) {
    	MorkParser parser = new MorkParser();
    	parser.setExceptionHandler(exceptionHandler);
        parser.setIgnoreTransactionFailures(true);
        parser.addEventListener(this);
        parser.parse(reader);
	}

    /**
     * Copy constructor, creates a copy of a document.
     * 
     * @param source
     */
//    public MorkDocument(MorkDocument source) {
//    	dicts
//    }
    
	/**
     * Internal
     */
    public void onEvent(Event event) {
        switch (event.eventType) {
        case END_DICT:
            {
                Dict dict = new Dict("<" + event.value + ">", dicts);
                dicts.addDictionary(dict);
                break;
            }
        case ROW:
            {
                Row row = new Row("[" + event.value + "]", dicts);
                rows.add(row);
                break;
            }
        case TABLE:
            {
                Table table = new Table("{" + event.value + "}", dicts);
                tables.add(table);
                break;
            }
        case GROUP_COMMIT:
            {
                MorkParser parser = new MorkParser();
                parser.addEventListener(this);
                parser.parse(new StringReader(event.value));
                break;
            }
        case END_METATABLE:
        case BEGIN_TABLE:
        case BEGIN_METATABLE:
        case BEGIN_DICT:
        case BEGIN_DICT_METAINFO:
        case END_DICT_METAINFO:
        case END_OF_FILE:
        case COMMENT:
        case CELL:
            break;
        default:
            throw new RuntimeException("Unimplemented event: " + 
                                       event.eventType + " for content " + event.value);
        }
    }

    /**
     * Returns all dictionaries found in the Mork document
     * 
     * @return a list of all dictionaries
     */
    public List<Dict> getDicts() {
        return dicts.getDictionaries();
    }

    /**
     * Returns a list of all rows which were not inherited in tables found in
     * the Mork document
     * 
     * @return a list of all rows which were not inherited in tables
     */
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Returns a list of tables
     * 
     * @return a list of tables found in the Mork document
     */
    public List<Table> getTables() {
        return tables;
    }

	public Row createRow() {

//		Map<String, Alias> aliasMap = new HashMap<String, Alias>();

		Set<String> rowIds = new HashSet<String>();
		for (Row row : rows) {
			String id = row.getRowId();
			rowIds.add(id);
		}

		int proposedRowNumber = 0;
		do {
			proposedRowNumber++;
		} while (rowIds.contains(Integer.toString(proposedRowNumber)));
		
		
		String proposedRowId = "[" + Integer.toString(proposedRowNumber) + "]";
			
		Row row = new Row(proposedRowId, dicts);
		rows.add(row);
		return row;
	}

	public void deleteRow(Row row) {
		rows.remove(row);
		
	}
}
