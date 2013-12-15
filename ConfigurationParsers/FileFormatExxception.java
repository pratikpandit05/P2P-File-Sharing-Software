
package ConfigurationParsers;

public class FileFormatExxception extends Exception {

        private static final long serialVersionUID = 1L;
        private String errorMessage;
        
        public FileFormatExxception(String errorMessage)
        {
                super(errorMessage);
                this.errorMessage = errorMessage;
        }
        
        public FileFormatExxception()
        {
                super();
                errorMessage = "Invalied";
        }
        
        public String getMessage()
        {
                return errorMessage;
        }
}

