package ninja.javafx.smartcsv.fx.table.model;

import ninja.javafx.smartcsv.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * unit test for the value class
 */
public class CSVValueTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static final String COLUMN = "COLUMN";
    static final String VALUE = "VALUE";


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // mocks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Validator validator = mock(Validator.class);


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // subject under test
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVValue sut;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // init
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Before
    public void initialize() {
        sut = new CSVValue();
        sut.setValidator(validator);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void set_value_calls_validation() {
        //setup
        sut.setColumn(COLUMN);

        // execution
        sut.setValue(VALUE);

        // assertion
        verify(validator).isValid(COLUMN, VALUE);
    }
}