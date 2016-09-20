import org.optionscity.uchicago.common.case1.Case1Ticker;
import org.optionscity.uchicago.job.case1.AbstractCase1Job;


public class ILL1Case1Job extends AbstractCase1Job {
    // record prices
    private double[] p_NAPA = new double[2000];
    private double[] p_NAPB = new double[2000];
    private double[] p_YBERA = new double[2000];
    private double[] p_YBERB = new double[2000];

    // record returns
    private double[] r_NAPA = new double[2000];
    private double[] r_NAPB = new double[2000];
    private double[] r_YBERA = new double[2000];
    private double[] r_YBERB = new double[2000];
    
    double pred_NAPB;
    double pred_YBERB;
    // double lastindicator1;
    // boolean tradablechange= false;
    int state = 0;
    // int signal=0;
    // int tradablesignal=0;
    int round;
    int tick = 0;
    int toFill;    
    int lock_NAPA;
    int lock_NAPB;
    int lock_YBERA;
    int lock_YBERB;
    int NAPA_hedge=0;
    int NAPB_hedge=0;
    int YBERA_hedge=0;
    int YBERB_hedge=0;
    boolean NAPA_tradable;
    boolean NAPB_tradable;
    boolean YBERA_tradable;
    boolean YBERB_tradable;
    // double N_state2_mvg;
    // double state2_mvg;
    
    //initialization methods for three rounds
    public void initialize1(){
    	if(tick%600 == 0){
    		NAPA_tradable = true;
        	NAPB_tradable = true;
        	YBERA_tradable = true;
        	YBERB_tradable = true;
        	toFill = 0;
            tick = 0;
        	log("initialize at :"+tick);
    	}
    }
    
    public void initialize2(){
    	if(tick%600 == 0){
    		NAPA_tradable = true;
        	NAPB_tradable = true;
        	YBERA_tradable = false;
        	YBERB_tradable = true;
        	toFill = 0;
            tick = 0;
            state = 0;
        	log("initialize at :"+tick);
    	}
    }
    
    public void initialize3(){
    	if(tick%600 == 0){
    		NAPA_tradable = true;
        	NAPB_tradable = true;
        	YBERA_tradable = true;
        	YBERB_tradable = true;
        	toFill = 0;
        	lock_NAPA = 0;
        	lock_NAPB = 0;
        	lock_YBERA = 0;
        	lock_YBERB = 0;
            tick = 0;
            state = 0;
        	log("initialize at :"+tick); 
    	}	 	
    }
    
    
    public void fillPriceAndReturn(){
    	p_NAPA[toFill] = getPrice(Case1Ticker.NAPA);
    	p_NAPB[toFill] = getPrice(Case1Ticker.NAPB);
        p_YBERA[toFill] = getPrice(Case1Ticker.YBERA);
        p_YBERB[toFill] = getPrice(Case1Ticker.YBERB);

        if(tick > 1){
        r_NAPA[toFill-1] = (getPrice(Case1Ticker.NAPA)-p_NAPA[toFill-1])/p_NAPA[toFill-1];
        r_NAPB[toFill-1] = (getPrice(Case1Ticker.NAPB)-p_NAPB[toFill-1])/p_NAPB[toFill-1];;
        r_YBERA[toFill-1] = (getPrice(Case1Ticker.YBERA)-p_YBERA[toFill-1])/p_YBERA[toFill-1];;
        r_YBERB[toFill-1] = (getPrice(Case1Ticker.YBERB)-p_YBERB[toFill-1])/p_YBERB[toFill-1];;  
        }
    }
    

    public void makeOrder_NAPB(){
            pred_NAPB = -0.5946*p_NAPA[toFill-2]+0.9166*p_NAPA[toFill-1]+0.0617*p_NAPA[toFill]+0.2715;

        //the order quantity needs to be changed
        if((pred_NAPB > p_NAPB[toFill]) && NAPB_tradable)
            order(Case1Ticker.NAPB, 500);
        else if((pred_NAPB < p_NAPB[toFill]) && NAPB_tradable)
        	order(Case1Ticker.NAPB, -500);     
    }
    public void makeOrder_NAPB_r2(){
        pred_NAPB = -0.5946*p_NAPA[toFill-2]+0.9166*p_NAPA[toFill-1]+0.0617*p_NAPA[toFill]+0.2715;

    //the order quantity needs to be changed
    if((pred_NAPB > p_NAPB[toFill]) && NAPB_tradable){
        order(Case1Ticker.NAPB, 500);
        order(Case1Ticker.YBERB, 500);}
    else if((pred_NAPB < p_NAPB[toFill]) && NAPB_tradable){
    	order(Case1Ticker.NAPB, -500);
        order(Case1Ticker.YBERB, -500); }
}
    public void makeOrder_YBERB(){

          pred_YBERB = 0.2251*p_YBERA[toFill-4]-0.1249*p_YBERA[toFill-3]-0.9174*p_YBERA[toFill-2]+1.1806*p_YBERA[toFill-1]+0.0214*p_YBERA[toFill]-0.0491;
    	
       if (round != 2){
    	   if((pred_YBERB > p_YBERB[toFill]) && YBERB_tradable)
    		   order(Case1Ticker.YBERB, 500);
    	   else if((pred_YBERB < p_YBERB[toFill]) && YBERB_tradable)
    		   order(Case1Ticker.YBERB, -500);
    	   }
    }
    
    //this is to clear all the position
    public void stop(){
    	order(Case1Ticker.NAPA, -getPosition(Case1Ticker.NAPA));
    	order(Case1Ticker.NAPB, -getPosition(Case1Ticker.NAPB));
    	order(Case1Ticker.YBERA, -getPosition(Case1Ticker.YBERA));
    	order(Case1Ticker.YBERB, -getPosition(Case1Ticker.YBERB));
    }
    

    public void onTick(){
    	round = Integer.parseInt(container.getVariable("round number"));
    	
    	if(round == 1)
    	{
    		//this is to sell out all the equities that we are currently holding
    		order(Case1Ticker.NAPB, -getPosition(Case1Ticker.NAPB));
    		order(Case1Ticker.YBERB, -getPosition(Case1Ticker.YBERB));
    		
            initialize1();
    		onTickRound1();
    	}	
    	else if (round == 2)
    	{	
            //this is to sell out all the equities that we are currently holding
    		order(Case1Ticker.NAPB, -(getPosition(Case1Ticker.NAPB)));
    		order(Case1Ticker.YBERB, -(getPosition(Case1Ticker.YBERB)));
    		
    		initialize2();
    		onTickRound2();
    	}
    	else
    	{
    		
    		//fillPriceAndReturn();
            //this is to sell out all the equities that we are currently holding
    		order(Case1Ticker.NAPB, -(getPosition(Case1Ticker.NAPB)));
    		order(Case1Ticker.YBERB, -(getPosition(Case1Ticker.YBERB)));
    		
    		initialize2();
    		onTickRound2();

    	}
    	
    }
    
    public void onTickRound1() {
    	log("round 1!");
    	tick++;
    //store initial values and do nothing
    	if(tick < 5){
    		fillPriceAndReturn();
    		if(tick >= 3){
    			makeOrder_NAPB();
    		}
    	}
    //if not, we have enough data to do prediction	
    	else if (tick<600)
    	{
    		fillPriceAndReturn();
    		makeOrder_NAPB();
    		makeOrder_YBERB();
    	}
    	toFill++;

       // double cang1=getPosition(Case1Ticker.NAPB);
       // double cang2=getPosition(Case1Ticker.YBERB);
       // log("position1"+cang1);
       // log("position2"+cang2);
    }
    
    public void onTickRound2() {
    	log("round 2!");
    	tick++;
    //store initial values and do nothing
    	if(tick < 3){
    		fillPriceAndReturn();
    	}
    //if not, we have enough data to do prediction	
    	else if (tick<600)
    	{
    		fillPriceAndReturn();
    		makeOrder_NAPB_r2();
    	}
    	toFill++;

       // double cang1=getPosition(Case1Ticker.NAPB);
       // double cang2=getPosition(Case1Ticker.YBERB);
       // log("position1"+cang1);
       // log("position2"+cang2);
    }
    

    public void onTickRound3() {
    	log("round 3!");

    	tick++;
    //store initial values and do nothing
    	if(tick < 5){
    		fillPriceAndReturn();
    		if(tick >= 3){
    			makeOrder_NAPB();
    		}
    	}
    //if not, we have enough data to do prediction	
    	else if(tick<600)
    	{
    		fillPriceAndReturn();
    		makeOrder_NAPB();
    		makeOrder_YBERB();
    	}
    	toFill++;

       }
}