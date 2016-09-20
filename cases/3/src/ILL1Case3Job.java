import org.optionscity.uchicago.common.case3.Case3Option;
import org.optionscity.uchicago.common.case3.Case3Ticker;
import org.optionscity.uchicago.common.case3.Greeks;
import org.optionscity.uchicago.common.case3.Market;
import org.optionscity.uchicago.job.case3.AbstractCase3Job;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ILL1Case3Job extends AbstractCase3Job {
    double deltaPos;
    double vegaPos;
    

    double currentDelta;
    double currentVega;
    double call_100;
    double call_80;
    double call_120;
    
    double put_100;
    double put_80;
    double put_120;
    
    int buy1=0;
    int buy2=0;
    int buy3=0;
    int buy4=0;
    int buy5=0;
    int buy6=0;
    int sell1=0;
    int sell2=0;
    int sell3=0;
    int sell4=0;
    int sell5=0;
    int sell6=0;
    int cc8=0;
    int cc10=0;
    int cc12=0;
    int cp8=0;
    int cp10=0;
    int cp12=0;
    int lc8=0;
    int lc10=0;
    int lc12=0;
    int lp8=0;
    int lp10=0;
    int lp12=0;
    int[] tick = new int[800];
    boolean[] fills = new boolean[800];
    
    double scale_option1_bid = 0.1;
    double scale_option2_bid = 0.1;
    double scale_option3_bid = 0.1;
    double scale_option4_bid = 0.1;
    double scale_option5_bid = 0.1;
    double scale_option6_bid = 0.1;
    
    double scale_option1_ask = 0.1;
    double scale_option2_ask = 0.1;
    double scale_option3_ask = 0.1;
    double scale_option4_ask = 0.1;
    double scale_option5_ask = 0.1;
    double scale_option6_ask = 0.1;
    double market_width=0.1;
    
    int long_tradable = 1;
    int short_tradable = 1;
    
    
    
    //int option_counter = 0;
    int tick_time = 0;
    int global_hit = 0;
    
    public void search_spread(Case3Option option)
    {
        //get the current option position
        
        //if get hit

        if( tick_time == 24 )
        {
            global_hit = buy1+buy2+buy3+buy4+buy5+buy6+sell1+sell2+sell3+sell4+sell5+sell6;
            log("hit"+global_hit);
            if( global_hit > 20 )
            {
                 scale_option1_bid = 0.2;
                 scale_option2_bid = 0.2;
                 scale_option3_bid = 0.2;
                 scale_option4_bid = 0.2;
                 scale_option5_bid = 0.2;
                 scale_option6_bid = 0.2;
                 scale_option1_ask = 0.2;
                 scale_option2_ask = 0.2;
                 scale_option3_ask = 0.2;
                 scale_option4_ask = 0.2;
                 scale_option5_ask = 0.2;
                 scale_option6_ask = 0.2;
                 market_width = 0.2;
                 log("scale"+scale_option1_bid);
            }
            
            else if( global_hit <= 4 )
            {
                 scale_option1_bid = 0.05;
                 scale_option2_bid = 0.05;
                 scale_option3_bid = 0.05;
                 scale_option4_bid = 0.05;
                 scale_option5_bid = 0.05;
                 scale_option6_bid = 0.05;
                 scale_option1_ask = 0.05;
                 scale_option2_ask = 0.05;
                 scale_option3_ask = 0.05;
                 scale_option4_ask = 0.05;
                 scale_option5_ask = 0.05;
                 scale_option6_ask = 0.05;
                 market_width = 0.05;
                 log("scale"+scale_option1_bid);
            }
         }
       
    }
    
    
    public void tradeOption(Case3Option option, double scale1, double scale2 ){
        Greeks Gs = getGreeks(option);
        double optionDelta = Gs.delta;
        double optionVega = Gs.vega;
        int    optionPosition = getPosition(option);
        
        double optionPrice = Gs.theoreticalPrice;
        

        if (long_tradable==1&short_tradable==1){
        Market market = new Market((optionPrice-scale1*optionPrice), (optionPrice+scale2*optionPrice));        
        makeMarket(option, market);}
        if (long_tradable==0){
        Market market = new Market(0, (optionPrice+scale2*optionPrice));        
        makeMarket(option, market);}
        if (short_tradable==0){
        Market market = new Market((optionPrice-scale1*optionPrice), Double.MAX_VALUE);        
        makeMarket(option, market);}
        if( tick_time <= 24 )
            search_spread(option);
        
        //option_counter++;
        
    }
    
    public void onTick() {
        
        //update the time tick
        tick_time++;
        
        Delta();
        currentVega = getTotalVega();
        if( currentVega>  300){long_tradable=0;}
        if( currentVega<  -300){short_tradable=0;}
        if( currentVega<  200){long_tradable=1;}
        if( currentVega>  -200){short_tradable=1;}
        

        
        //get the buy and sell at current tick time

        scale_option1_bid=getscaled(currentVega)[0];
        log("c11"+scale_option1_bid);
        scale_option1_ask=getscaled(currentVega)[1];
        log("c12"+scale_option1_ask);

        List<String> options=topVegas();
        //log(""+options.get(0)+options.get(1)+options.get(2)+options.get(3)+options.get(4)+options.get(5));
        //log("size: "+options.size());
        log("c22"+scale_option2_ask);
        tradeOption(strtooption(options.get(0)),scale_option1_bid, scale_option1_ask);
        tradeOption(getparity(options.get(0)),scale_option1_bid, scale_option1_ask);
        tradeOption(strtooption(options.get(1)),scale_option1_bid, scale_option1_ask);
        tradeOption(getparity(options.get(1)),scale_option2_bid, scale_option2_ask);
        tradeOption(strtooption(options.get(2)),scale_option2_bid, scale_option2_ask);
        tradeOption(getparity(options.get(2)),scale_option2_bid, scale_option2_ask);
        record();

      
        
    }
    
    public void record(){
    
        cc8=getPosition(Case3Option.CALL_80);
        cc10=getPosition(Case3Option.CALL_100);
        cc12=getPosition(Case3Option.CALL_120);
        cp8=getPosition(Case3Option.PUT_80);
        cp10=getPosition(Case3Option.PUT_100);
        cp12=getPosition(Case3Option.PUT_120);
        
        if((cc8-lc8)>0){buy1=buy1+(cc8-lc8);}
        if((cc10-lc10)>0){buy2=buy2+(cc10-lc10);}
        if((cc12-lc12)>0){buy3=buy3+(cc12-lc12);}
        if((cp8-lp8)>0){buy4=buy4+(cp8-lp8);}
        if((cp10-lp10)>0){buy5=buy5+(cp10-lp10);}
        if((cp12-lp12)>0){buy6=buy6+(cp12-lp12);}
        
        if((cc8-lc8)<0){sell1=sell1-(cc8-lc8);}
        if((cc10-lc10)<0){sell2=sell2-(cc10-lc10);}
        if((cc12-lc12)<0){sell3=sell3-(cc12-lc12);}
        if((cp8-lp8)<0){sell4=sell5-(cp8-lp8);}
        if((cp10-lp10)<0){sell5=sell5-(cp10-lp10);}
        if((cp12-lp12)<0){sell6=sell6-(cp12-lp12);}
        
        lc8=cc8;
        lc10=cc10;
        lc12=cc12;
        lp8=cp8;
        lp10=cp10;
        lp12=cp12;
    }

    public double[] getscaled(double vegga){
        

        double scale[] = new double[2];

       
            if (vegga>=200)
            {
                scale[0]= market_width*2;
                scale[1]= market_width*0.1;
            }
            else if(vegga<200 && vegga>100)
            {
                scale[0]= market_width*1.7;
                scale[1]= market_width*0.3;
            }
            else if(vegga<=100 && vegga>=-100)
            {
                scale[0]= market_width;
                scale[1]= market_width;
            }
            else if(vegga<-100 && vegga>-200)
            {
                scale[0]= market_width*0.3;
                scale[1]= market_width*1.7;
            }
            else if(vegga<=-200)
            {
                scale[0]= market_width*0.1;
                scale[1]= market_width*2;
            }
 
     
        return scale;
    }

    
    public void Delta(){
        
        currentDelta = getTotalDelta();
        log("Delta"+currentDelta);
        
        if(currentDelta > 100)
        {
            order(Case3Ticker.UCHIX, (int)((-currentDelta)/100));
            log("DNeutral-underlying"+currentDelta);
            log("DNeutral-shares"+(int)((-currentDelta)/100));
        }
        
        else if (currentDelta < -100)
        {
            order(Case3Ticker.UCHIX, (int)(-currentDelta/100));
            log("DNeutral+underlying"+currentDelta);
            log("DNeutral-shares"+(int)((currentDelta)/100));
        }

    }
    public Case3Option strtooption(String str){
    	if (str=="call80"){return Case3Option.CALL_80;}
    	if (str=="call100"){return Case3Option.CALL_100;}
    	if (str=="call120"){return Case3Option.CALL_120;}
    	if (str=="put80"){return Case3Option.PUT_80;}
    	if (str=="put100"){return Case3Option.PUT_100;}
    	if (str=="put120"){return Case3Option.PUT_120;}
    	return Case3Option.CALL_80;
    }
    public Case3Option getparity(String str){
    	if (str=="call80"){return Case3Option.PUT_80;}
    	if (str=="call100"){return Case3Option.PUT_100;}
    	if (str=="call120"){return Case3Option.PUT_120;}

    	return Case3Option.CALL_80;
    }
     public List <String> topVegas(){
    	

    	double call80 = getGreeks(Case3Option.CALL_80).vega;
    	double call100 = getGreeks(Case3Option.CALL_100).vega;
    	double call120 = getGreeks(Case3Option.CALL_120).vega;
    	double put80 = getGreeks(Case3Option.PUT_80).vega;
    	double put100 = getGreeks(Case3Option.PUT_100).vega;
    	double put120 = getGreeks(Case3Option.PUT_120).vega;
    	

    	HashMap <Double, String> vegaToTicket = new HashMap <Double, String>();

    	vegaToTicket.put(call80,"call80");
    	vegaToTicket.put(call100,"call100");
    	vegaToTicket.put(call120,"call120");

    	
    	double [] vegas = new double [3];
    	
    	//List<Double> sortedKeys = new ArrayList<Double>();
//    	sortedKeys.add(0,call80);
//    	sortedKeys.add(1,call100);
//    	sortedKeys.add(2,call120);
//    	sortedKeys.add(3,put80);
//    	sortedKeys.add(4,put100);
//    	sortedKeys.add(5,put120);
    	
//    	Collections.sort(sortedKeys);

    	vegas[0]=call80;
        vegas[1]=call100;
        vegas[2]=call120;	
//        vegas[3]=put80;
//        vegas[4]=put100;
//        vegas[5]=put120;
        //log(""+vegas[0]+" "+vegas[1]+" "+vegas[2]+" "+vegas[3]+" "+vegas[4]+" "+vegas[5]);
        sort(vegas);
        //log(""+vegas[0]+" "+vegas[1]+" "+vegas[2]+" "+vegas[3]+" "+vegas[4]+" "+vegas[5]);
    	//log(""+sortedKeys.get(0)+" "+sortedKeys.get(1)+" "+sortedKeys.get(2)+" "+sortedKeys.get(3)+" "+sortedKeys.get(4)+" "+sortedKeys.get(5));

    	List<String> ret = new ArrayList <String>();

    	for(int i = 0; i < 3; i++)
    	{
    		//log(""+vegaToTicket.get(sortedKeys.get(5-i))+""+sortedKeys.get(5-i));
    		ret.add(i,vegaToTicket.get(vegas[2-i]));
    		log("size: "+ret.size());
    	}
//    	
//    	log(""+ret.get(0)+" "+ret.get(1)+" "+ret.get(2)+" "+ret.get(3)+" "+ret.get(4)+" "+ret.get(5));
    	

    	return ret;
    }
     public void sort(double[] x) {
    	  boolean sorted=true;
    	  double temp;

    	  while (sorted){
    	     sorted = false;
    	     for (int i=0; i < x.length-1; i++) 
    	        if (x[i] > x[i+1]) {                      
    	           temp       = x[i];
    	           x[i]       = x[i+1];
    	           x[i+1]     = temp;
    	           sorted = true;
    	        }          
    	  } }
    public void onVegaLimitBreached() {
        
    }

    public void onDeltaLimitBreached() {
    }
}