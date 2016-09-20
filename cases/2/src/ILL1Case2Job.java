
import org.optionscity.uchicago.common.case2.Case2Ticker;
import org.optionscity.uchicago.common.case2.CounterParty;
import org.optionscity.uchicago.job.case2.AbstractCase2Job;

public class ILL1Case2Job extends AbstractCase2Job {
    
    //order object
    double dev_A;
    double dev_B;
    double dev_C;
    double mean_A;
    double mean_B;
    double mean_C;
    int n_A;
    int n_B;
    int n_C;
    double c_idx=0;
    double l_idx=0;
    double JLG_iv=0.004284784;
    double ML_iv=0.01369402;
    double OIL_iv=0.004269644;
    double WTR_iv=0.000819867;
    double JLG_bv=0.0077018;
    double ML_bv=0.0089022691;
    double OIL_bv=0.0049451534;
    double WTR_bv=0.000895622;
    int JLG_close=0;
    int ML_close=0;
    int OIL_close=0;
    int WTR_close=0;
    int rec=5;
    int rec_time=0;
    int ticks = 0;
    int round;
    int get_order=0;
    double idx_change;
    int H=0;
    int M=0;
    int stop=0;
    
    public void onTick() {
        round = Integer.parseInt(container.getVariable("round number"));
        M=0;
        close();
        ticks++;
        if (ticks>591){
            stop=1;
        }
        if (round==2 ){
        H=(ticks-rec)%3;
        if (H>0){
            betahedge(Case2Ticker.UCX,0,JLG_close,ML_close,OIL_close,WTR_close);            
        }
        }
        if ((round==1)||(round==3)){
            H=(ticks-rec)%5;
            if (H>0){
                betahedge(Case2Ticker.UCX,0,JLG_close,ML_close,OIL_close,WTR_close);            
            }
        }
        log("tick"+ticks);
        log("orders get "+get_order);
        log("rec"+rec);
    }
//   record the activity of counterparty
    public void record(Case2Ticker ticker_r, CounterParty counterParty_r, double price_r){
        if(counterParty_r==CounterParty.A){
            n_A++;
            dev_A=dev_A+(price_r-getPrice(ticker_r))/getPrice(ticker_r);
            mean_A=dev_A/n_A;
        }
        if(counterParty_r==CounterParty.B){
            n_B++;
            dev_B=dev_B+(price_r-getPrice(ticker_r))/getPrice(ticker_r);
            mean_B=dev_B/n_B;
        }
        if(counterParty_r==CounterParty.C){
            n_C++;
            dev_C=dev_C+(price_r-getPrice(ticker_r))/getPrice(ticker_r);
            mean_C=dev_C/n_C;
        }       
    }


//    a map from ticker to beta
    public double giveBeta(Case2Ticker ticker){
        if (ticker==Case2Ticker.UCX){return 1;}
        if (ticker==Case2Ticker.ML){return 2;}
        if (ticker==Case2Ticker.JLG){return 1.5;}
        if (ticker==Case2Ticker.OIL){return 0.9;}
        if (ticker==Case2Ticker.WTR){return 0.2;}
        return -1;
    }
    

    
    public boolean onPrivateOrder(Case2Ticker ticker, CounterParty counterParty, int quantity, double price) {
         double ticker_current_price = getPrice(ticker);
         double profit;
         if (rec_time==0){rec=ticks;}
         rec_time++;
         int closed_position=0;
         if (ticker==Case2Ticker.JLG){closed_position=JLG_close;}
         if (ticker==Case2Ticker.ML){closed_position=ML_close;}
         if (ticker==Case2Ticker.OIL){closed_position=OIL_close;}
         if (ticker==Case2Ticker.WTR){closed_position=WTR_close;}
         int order_value=0;
         int choose=0;
         record(ticker,counterParty,price);
         if(stop==1){return false;}
         if (quantity>0){
         profit = (price - ticker_current_price+0.5)*Math.abs(quantity);}
         else{
             profit =(ticker_current_price-price+0.5)*Math.abs(quantity);
         }

        if(profit>(getrisk(ticker,quantity,getPrice(ticker))[0]+getrisk(ticker,quantity,getPrice(ticker))[1])){
            log("profit"+profit+"risk"+getrisk(ticker,quantity,getPrice(ticker))[0]+"cost"+getrisk(ticker,quantity,getPrice(ticker))[1]);
            log("quantity"+quantity);
            get_order++;
            if (quantity>0){
                order_value=Math.min(1000,quantity);
                order_value=Math.min(1000,(order_value+closed_position))-closed_position;
                order(ticker,order_value);                
            }
            else if (quantity<0){
                order_value=-Math.min(1000,-quantity);
                order_value=Math.max(-1000,(order_value+closed_position))-closed_position;
                order(ticker,order_value);
            }
             
            if (M==0){betahedge(ticker,order_value-quantity,JLG_close,ML_close,OIL_close,WTR_close);}
            M++;
            return true;

            
        }
        

            return false;      
}
    

    public void close(){
        int idx_position=getPosition(Case2Ticker.UCX);
        int JLG_position=getPosition(Case2Ticker.JLG);
        int ML_position=getPosition(Case2Ticker.ML);
        int OIL_position=getPosition(Case2Ticker.OIL);
        int WTR_position=getPosition(Case2Ticker.WTR);
        JLG_close=0;
        ML_close=0;
        OIL_close=0;
        WTR_close=0;
        if (JLG_position>0){
            JLG_close=-Math.min(1000,JLG_position);
            order(Case2Ticker.JLG,-Math.min(1000,JLG_position));
            log("JLG_close"+JLG_close);
        }
        else if (JLG_position<0){
            JLG_close=Math.min(1000,-JLG_position);
            order(Case2Ticker.JLG,Math.min(1000,-JLG_position));
            log("JLG_close"+JLG_close);
        }
        if (ML_position>0){
            ML_close=-Math.min(1000,ML_position);
            order(Case2Ticker.ML,-Math.min(1000,ML_position));
            log("ML_close"+ML_close);
        }
        else if (ML_position<0){
            ML_close=Math.min(1000,-ML_position);
            order(Case2Ticker.ML,Math.min(1000,-ML_position));
            log("ML_close"+ML_close);
        }
        if (OIL_position>0){
            OIL_close=-Math.min(1000,OIL_position);
            order(Case2Ticker.OIL,-Math.min(1000,OIL_position));
            log("OIL_close"+OIL_close);
        }
        else if (OIL_position<0){
            OIL_close=Math.min(1000,-OIL_position);
            order(Case2Ticker.OIL,Math.min(1000,-OIL_position));
            log("OIL_close"+OIL_close);
        }
        if (WTR_position>0){
            WTR_close=-Math.min(1000,WTR_position);
            order(Case2Ticker.WTR,-Math.min(1000,WTR_position));
            log("WTR_close"+WTR_close);
        }
        else if (WTR_position<0){
            WTR_close=Math.min(1000,-WTR_position);
            order(Case2Ticker.WTR,Math.min(1000,-WTR_position));
            log("WTR_close"+WTR_close);
        }
    }
    
   public double[] getrisk(Case2Ticker ticker_g,int quantity_g,double price_g){
       double g_risk=0;
       double g_cost=0;
       double vol=0;
       double[] ret = new double[2];
       quantity_g=Math.abs(quantity_g);
       g_cost=0.25*Math.pow(1.001,1000)*1000;
       quantity_g=quantity_g-1000;
       if (round==3){
           JLG_iv=0.005141739;   
           ML_iv=0.016432871;
           OIL_iv=0.005123574;
           WTR_iv=0.001046383;
           JLG_bv=0.008012048;
           ML_bv=0.010682731;
           OIL_bv=0.005934189;
           WTR_bv=0.001074747;
       }
       if (ticker_g==Case2Ticker.JLG){vol=Math.sqrt(Math.pow(JLG_iv,2)+Math.pow(0.5*JLG_bv,2));}
       if (ticker_g==Case2Ticker.ML){vol=Math.sqrt(Math.pow(ML_iv,2)+Math.pow(0.5*ML_bv,2));}
       if (ticker_g==Case2Ticker.OIL){vol=Math.sqrt(Math.pow(OIL_iv,2)+Math.pow(0.5*OIL_bv,2));}
       if (ticker_g==Case2Ticker.WTR){vol=Math.sqrt(Math.pow(WTR_iv,2)+Math.pow(0.5*WTR_bv,2));}
//     int t=0;
//     
//     t = (int)(quantity_g/1000);
       while (quantity_g>1000){
           g_risk=g_risk+quantity_g*price_g*vol;
           quantity_g=quantity_g-1000;
           g_cost=g_cost+0.25*Math.pow(1.001,1000)*1000;
       }
       g_risk=g_risk+quantity_g*price_g*vol;
       g_cost=g_cost+0.25*Math.pow(1.001,quantity_g)*quantity_g;
       ret[0]=g_risk;
       ret[1]=g_cost;
       return ret;
   }
    
    
   
   

    
    public double onCompetitiveOrder(Case2Ticker case2Ticker, CounterParty counterParty, int i) {
        double pred=-1;
        double profit=0;
        double my_price;
        if (rec_time==0){rec=ticks;}
        rec_time++;
        if(stop==1){return -1;}
        if (counterParty==CounterParty.B){
            if (ticks>150){
                return comp_helper(case2Ticker,i,mean_B);
                }
            else{
                return comp_helper(case2Ticker,i,0);
            }
        }
        if (counterParty==CounterParty.C){
            if (ticks>150){
                return comp_helper(case2Ticker,i,mean_C);
                }
            else{
                return comp_helper(case2Ticker,i,0);
            }
        }
        if (counterParty==CounterParty.A){
            if (ticks>150){
                return comp_helper(case2Ticker,i,mean_A);
                }
            else{
                return comp_helper(case2Ticker,i,0);
            }
        }
        return -1;
    }
   public double comp_helper(Case2Ticker ticker, int quant, double mean){
     //sell
       double pred=-1;
       double profit=0;
       double my_price;
        if (quant >0){
        my_price=(getrisk(ticker,quant,getPrice(ticker))[0]+getrisk(ticker,quant,getPrice(ticker))[1])/quant-0.5+getPrice(ticker);
        pred=getPrice(ticker)*(1+mean);
        if (my_price>pred){return my_price;}
        else {return pred;}

        }
        //buy
        else{
        my_price=getPrice(ticker)-(getrisk(ticker,quant,getPrice(ticker))[0]+getrisk(ticker,quant,getPrice(ticker))[1])/(-quant)+0.5;
        pred=getPrice(ticker)*(1+mean);
        if (my_price<pred){return my_price;}
        else {return pred;}
        }
//      return -1;
   }
    
  public void betahedge(Case2Ticker h_ticker,int quantity,int q1, int q2, int q3, int q4){
        int idx_position=getPosition(Case2Ticker.UCX);
        int JLG_position=getPosition(Case2Ticker.JLG);
        int ML_position=getPosition(Case2Ticker.ML);
        int OIL_position=getPosition(Case2Ticker.OIL);
        int WTR_position=getPosition(Case2Ticker.WTR);
        double idx_price=getPrice(Case2Ticker.UCX);
        double JLG_price=getPrice(Case2Ticker.JLG);
        double ML_price=getPrice(Case2Ticker.ML);
        double OIL_price=getPrice(Case2Ticker.OIL);
        double WTR_price=getPrice(Case2Ticker.WTR);
        double idx_beta=giveBeta(Case2Ticker.UCX);
        double JLG_beta=giveBeta(Case2Ticker.JLG);
        double ML_beta=giveBeta(Case2Ticker.ML);
        double OIL_beta=giveBeta(Case2Ticker.OIL);
        double WTR_beta=giveBeta(Case2Ticker.WTR);
        if (h_ticker==Case2Ticker.JLG){q1=quantity;}
        if (h_ticker==Case2Ticker.ML){q2=quantity;}
        if (h_ticker==Case2Ticker.OIL){q3=quantity;}
        if (h_ticker==Case2Ticker.WTR){q4=quantity;}
        
        JLG_position=JLG_position+q1;
        ML_position=ML_position+q2;
        OIL_position=OIL_position+q3;
        WTR_position=WTR_position+q4;
        double port_value=idx_position*idx_price+JLG_position*JLG_price+OIL_position*OIL_price+WTR_position*WTR_price+ML_position*ML_price;
        double easy_beta= idx_position*idx_price*idx_beta+JLG_position*JLG_price*JLG_beta+ML_position*ML_price*ML_beta+OIL_position*OIL_price*OIL_beta+WTR_position*WTR_price*WTR_beta;
        log("easybeta"+easy_beta);
        log("beta"+(easy_beta/(port_value+1)));
        double new_value;
        double new_easy_beta;
        int i_quant;
        int j_quant;
        
        if (easy_beta>500){
            new_easy_beta=easy_beta-1000*idx_price*idx_beta;
            if (new_easy_beta<0){

                        i_quant=(int)(easy_beta/(idx_price*idx_beta));
                        order(Case2Ticker.UCX,-i_quant);
                        log("hedge11"+i_quant); 
            }
            else{
                order(Case2Ticker.UCX,-1000);
                log("hedge12"); 
            }
        }
        else if (easy_beta<-500){
            new_easy_beta=easy_beta+1000*idx_price*idx_beta;
            if (new_easy_beta>0){               
                        i_quant=(int)(-easy_beta/(idx_price*idx_beta));
                        order(Case2Ticker.UCX,i_quant);
                        log("hedge21"+i_quant); 
            }
            else{
                order(Case2Ticker.UCX,1000);
                log("hedge12"); 
            }
        }
    }

}