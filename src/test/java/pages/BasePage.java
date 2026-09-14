package pages;
import com.microsoft.playwright.Page;
import static hooks.Hooks.getPage;
public class BasePage {


public void navegar(String url) {
    Page page = getPage();
    page.navigate(url);
}

public void title(String titulo){
    Page page = getPage();
    page.title();
    
    
    
}



}
