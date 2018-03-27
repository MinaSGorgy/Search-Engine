package Database;

import Indexer.Document;
import Indexer.Word;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.SqlCommand;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DatabaseController implements Closeable {
    DatabaseConnector connector;
    DocumentDatabaseModule documentModule;
    WordDatabaseModule wordModule;
    WordListDatabaseModule wordsModule;

    public DatabaseController() throws SQLException {
        connector = new DatabaseConnector();
        documentModule = new DocumentDatabaseModule(connector);
        wordModule = new WordDatabaseModule(connector);
        wordsModule = new WordListDatabaseModule(connector);
    }

    @Override
    public void close() {
        connector.close();
    }

    public void insertDocument(Document document) {
        try {
            documentModule.insert(document);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in Inserting document");
        }
    }

    public void updateDocument(Document document) {
        try {
            documentModule.update(document);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in updating document");
        }
    }

    public void deleteDocument(Document document) {
        try {
            documentModule.delete(document);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in deleting document");
        }
    }

    public void insertWord(Word word) {
        try {
            wordModule.insert(word);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in inserting new word");
        }
    }

    public void updateWord(Word word) {
        try {
            wordModule.update(word);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in updating word");
        }
    }

    public void deleteWord(Word word) {
        try {
            wordModule.delete(word);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in deleting word");
        }
    }

    public void insertWords(List<Word> words) {
        try{
            wordsModule.copy(words);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in inserting a list of words");
        }

    }

    public void updateWords(List<Word> words) {
        try {
            wordsModule.update(words);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in updating list of words");
        }
    }

    public void deleteWords(List<Word> words) {
        try {
            wordsModule.delete(words);
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in deleting list of words");
        }
    }

    public List<Document> getUnprocessedDocuments() {
        try {
            return documentModule.select();
        } catch (SQLException exception) {
            handleSQLException(exception, "Error in getting unprocessed documents");
        }
        return null;
    }

    private void handleSQLException(SQLException exception, String message) {
        System.err.println(message);
        System.err.println(exception.getSQLState());
        System.err.println(exception.getMessage());
    }

}
