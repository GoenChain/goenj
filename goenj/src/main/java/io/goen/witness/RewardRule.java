package io.goen.witness;/**
 * Created by yuanhangzhang1 on 2018/5/24.
 */

import com.medici.firestar.witness.common.CurveEnum;

import java.math.BigInteger;

/**
 * @author yuanhangzhang1
 * @create 2018-05-24 8:28
 **/
public class RewardRule {

    public static BigInteger evaluateRewardCurve(BigInteger rshares, CurveEnum curve, BigInteger contentConstant ) {

        BigInteger result = new BigInteger("0");

        switch( curve )
        {
//            case QUADRATIC:
//                {
//                    BigInteger rshares_plus_s = rshares.add(contentConstant);
//                    result = rshares_plus_s*rshares_plus_s - contentConstant * contentConstant;
//                }
//                break;
//            case QUADRATIC_CURATION:
//                {
//                    BigInteger two_alpha = content_constant * 2;
//                    result = uint128_t( rshares.lo, 0 ) / ( two_alpha + rshares );
//                }
//                break;
            case LINEAR:
                result = rshares;
                break;
            case SQUARE_ROOT:
                result = new BigInteger( approx_sqrt( rshares.toString() ) ) ;
                break;
            default:
                break;
        }

        return result;
    }


    public static String approx_sqrt(String num)
    {
        BigInteger b=new BigInteger(num);
        if(b.compareTo(BigInteger.ZERO)<0) {
            return "0";
        }

        String sqrt="0"; //
        String pre="0"; //
        BigInteger trynum; //
        BigInteger flag;  //
        BigInteger _20=new BigInteger("20"); //20
        BigInteger dividend; ///
        BigInteger A;  //(10*A+B)^2=M
        BigInteger B;
        BigInteger BB;

        int len=num.length(); //

        if(len%2==1)  //10
        {
            num="0"+num;
            len++;
        }

        for(int i=0;i<len/2;++i) //len/2
        {
            dividend=new BigInteger(pre+num.substring(2*i,2*i+2));
            A=new BigInteger(sqrt);
            for(int j=0;j<=9;++j)
            {
                B=new BigInteger(j+"");
                BB=new BigInteger((j+1)+"");

                trynum=_20.multiply(A).multiply(B).add(B.pow(2));
                flag=_20.multiply(A).multiply(BB).add(BB.pow(2));;

                //j
                if(trynum.subtract(dividend).compareTo(BigInteger.ZERO)<=0
                        &&flag.subtract(dividend).compareTo(BigInteger.ZERO)>0)
                {
                    sqrt+=j;  //j
                    pre=dividend.subtract(trynum).toString(); //
                    break;
                }
            }
        }
        return sqrt.substring(1);
    }

//    public static void main(String[] args) {
//        System.out.println(approx_sqrt("121"));
//        System.out.println(Math.sqrt(121.0));
//    }
}
