//Citation: http://www.javapractices.com/topic/TopicAction.do?Id=62
/*
Generate pseudo-random floating point values, with an 
approximately Gaussian (normal) distribution.

Many physical measurements have an approximately Gaussian 
distribution; this provides a way of simulating such values. 
*/
import java.util.Random;

public final class RandomGaussian {
    
  
  public static  double getGaussian(double aMean, double aVariance){
      Random fRandom = new Random();
      return aMean + fRandom.nextGaussian() * aVariance;
  }

} 