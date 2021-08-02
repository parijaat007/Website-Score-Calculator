# Website Score Calculator

## Simple Java script to calculate website score.
### Using Selenium and Webdriver

To change sites to be evaluated, change array strArray on line 71 according to requirements.

Website score may be defined as follows-   
  A: normalized Average Link Load Time (Mobile Network)  
  A = (val(col1) - min(col1)) / (max(col1) - min(col1))  
  B: Fraction of working links  
  B = val(col3) / (val(col2) + val(col3))  
  where col1=Average Link Load Time  
  col2=Number of dead links/Time Outs  
  col3=Number of working links  
Thus,  
  Website score = (A + B) / 2  
