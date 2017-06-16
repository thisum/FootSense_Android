package com.thisum.med.foolsim;

/**
 * Created by thisum on 4/19/2017.
 */

public interface ResultsNotifier
{
    public void notifyResults( Constants.Application application, String... msg );
}
