package Opera1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;




public class CheapOperaFinder {
	
	static String username = "YourUsername";
	static String password = "YourPassword";
	static int PRICE_LIMIT = 31;
	

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		
		
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        driver.get("http://www.operadeparis.fr");
       
        WebElement sidentifier = driver.findElement(By.id("onp_anonym_btn"));
        
        sidentifier.click();

        driver.switchTo().frame("IframeSSO");
        
        WebElement usernameElement = driver.findElement(By.name("username"));
        WebElement passwordElement = driver.findElement(By.name("password"));
        
        usernameElement.sendKeys(username);
        passwordElement.sendKeys(password);

        passwordElement.submit();

        driver.switchTo().defaultContent();
        
        Thread.sleep(5000);
        
        // go to see the operas page
        driver.findElement(By.xpath(".//*[@id='onp_n1']/li[2]/a")).click();        
        driver.findElement(By.xpath(".//*[@id='menu1415']/ul/li[1]/a/span")).click();

        // get all the "make reservation" button
        List<WebElement> reservers = driver.findElements(By.xpath(".//*[@id='genre_liste']/div/div/div[*]/div[6]/span/a"));
        
        String baseTab = driver.getWindowHandle();
        
        // open a new tab for each opera
        for(WebElement reserver : reservers){
        	Actions newTab = new Actions(driver);
        	newTab.keyDown(Keys.SHIFT).click(reserver).keyUp(Keys.SHIFT).build().perform();
        	Thread.sleep(10);
        }
        
        Set<String> tabsForEachOpera = driver.getWindowHandles();
        tabsForEachOpera.remove(baseTab);
        		
        
        for(String tab : tabsForEachOpera){

        	//swich to reservation page of each opera
        	driver.switchTo().window(tab);

        	List<WebElement> makeRs = driver.findElements(By.xpath(".//*[starts-with(@id, 'book')]/span[2]"));
        	//opens a new tab for each date of a opera
        	for(WebElement r : makeRs){
				Actions newTab = new Actions(driver);
				newTab.contextClick(r).sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.RETURN).build().perform();
        	}
        	driver.close();
        	

            boolean foundSomethingYetForThisOpera = false;
            
            for(int tabNb=0;tabNb<makeRs.size();tabNb++){
            	
            	//first we check we are really on a "select date" page (it could not be the cause because of subtle R button bug)
            	String pageType = driver.findElement(By.xpath(".//*[@id='breadcrumb_content_top']/span[4]/span")).getText();
            	
            	if (pageType.equals("Choix des places")) {
					int numberOfCategories = driver
							.findElements(
									By.xpath(".//*[starts-with(@id, 'EventFormModel')]/div[2]/table/tbody/tr[*]/td[5]"))
							.size();
					boolean foundSomethingYetForThisDate = false;
					for (int cat = 1; cat <= numberOfCategories; cat++) {
						String isFullOrNot = driver
								.findElement(
										By.xpath(".//*[@id='EventFormModel']/div[2]/table/tbody/tr["
												+ cat + "]/td[1]")).getText();
						String price = driver
								.findElement(
										By.xpath(".//*[@id='EventFormModel']/div[2]/table/tbody/tr["
												+ cat + "]/td[5]")).getText();

						//matcher for the price
						String regex = "(\\d*)\\.";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(price);
						matcher.find();
						int priceInt = Integer.parseInt(matcher.group(1));

						if (priceInt <= PRICE_LIMIT
								&& !isFullOrNot.contains("Epuisé")) {
							if (!foundSomethingYetForThisOpera) {
								String place = driver
										.findElement(
												By.xpath(".//*[starts-with(@id, 'prod')]/div[1]/div/span[1]/span"))
										.getText();
								String title = driver
										.findElement(
												By.xpath(".//*[starts-with(@id, 'prod')]/div[1]/span[1]"))
										.getText();
								foundSomethingYetForThisOpera = true;
								System.out
										.println("----------------------------------------\n----------------------------------------");
								System.out
										.println(title + " - " + place + "\n");
							}
							if (!foundSomethingYetForThisDate) {
								String date = driver
										.findElement(
												By.xpath(".//*[starts-with(@id, 'prod')]/div[1]/span[3]/span/span[1]"))
										.getText();
								String time = driver
										.findElement(
												By.xpath(".//*[starts-with(@id, 'prod')]/div[1]/span[3]/span/span[3]"))
										.getText();
								foundSomethingYetForThisDate = true;
								System.out.println(date + " - " + time);
							}
							System.out.println(isFullOrNot + " " + price);
						}
					}
				}
				// change tab
                driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
                driver.switchTo().window(tab);
            }
        	
        }
        
        driver.switchTo().window(baseTab);
        driver.quit();
       


	}

}
