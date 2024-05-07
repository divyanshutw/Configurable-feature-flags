# ❗Problem statement:
I want to run a logic at the backend and frontend that is based on an experiment. By experiment, I mean that there can be two different logics (say data science algorithms, etc.) and I want to run experiment 1 for a certain set of users and experiment 2 for the remaining set of users. This is called running A/B experiments in general or implementing feature flags.

It can also be understood as a beta program. For some users, the beta experiment will run and the remaining users will continue to see the older stable version.

Or there might be another scenario where the team is confused about 2 different data science algorithms or 2 different frontend designs. So they can ask us to run one algorithm for 50% of users and another algorithm for the remaining 50% of users. Note that there can be more than two sets of variations too.

So the problem is to run K different experiments with N1,N2,N3,….Nk no. of logics respectively and the division percentage has to be configurable, i.e., anyone else can easily change the configurations of the traffic division. Also, the session of one user has to be sticky, i.e., if a user is viewing experiment A in a browser session, he should be shown the same experiment everytime in that session. Don’t blow your mind 🤯 reading the problem, I’ve got you covered.

# 💡Idea:
Now the constraint here is to identify a particular user. I need to identify a user(or browser session) because if two experiments are going on: A and B, I want that in a particular session, if experiment A is being shown, then throughout the session experiment A should be shown. It shouldn’t happen that a user reloads his browser tab and he is seeing experiment B now. This is called a sticky session in general.

There can be several ways to identify a user. We can use the user’s IP or a unique transaction-id for example. In my case, I have used the timestamp when the session was first opened. If the timestamp cookie is not received in the header, it is set otherwise the timestamp cookie received in the request header is used. Whenever the user first opens his session, their timestamp is stored as a cookie in their browser session.

Based on this timestamp, I decide at runtime which experiments the user should be shown. This is done by simple probabilistic distribution. Next time, when the flow is executed in the same session, the same timestamp cookie is received and the probabilistic distribution would give the same experiment. This way, the sticky session is also maintained.

Now, let’s delve into the implementation details in the code.🧑‍💻

# ✍️Low level design:
Here is the class diagram of the service depicting all important classes, models and entities:

![Low level design pic1](https://miro.medium.com/v2/resize:fit:720/format:webp/0*3pfrDdaeM72twK9S)
![Low level design pic2](https://miro.medium.com/v2/resize:fit:720/format:webp/0*dGdbOETwzZVR3PvB)

Here are some details of the project:

🟡 Firstly there are simple CRUD APIs to create(/create), update(/updateExp) and get(/getExp) experiments and their respective configuration.

🟡 I have used simple h2 database for this example but it has to be replaced by the project’s mysql server configurations. Following properties have to be changed for this purpose:

spring.datasource.url=jdbc:h2:file:~/data/divDB;DB_CLOSE_ON_EXIT=TRUE;DATABASE_TO_UPPER=true;IFEXISTS=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=user
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.defer-datasource-initialization=true
🟡 There is also an API(/checkExpType) that illustrates how one can use the experiment in an API.

🟡 The main logic is written in ExpIdentifierFilter.java class. This is a PreRequestFilter and is executed in every API request that comes in this application. Let’s go through the logic in this class.

# ExpIdentifierFilter.java
private long getExpIdentifier(HttpServletRequest httpServletRequest)
This function returns the unique experiment identifier. In the first browser session, no “expId” cookie is received and so this function returns the current epoch timestamp. Then I set the “expId” cookie as that timestamp. In all further API calls in that browser session, this cookie will be received in the header and returned by this function.

> IExpIdentifierGetService expIdentifierGetService = expIdentifierGetFactory.getExpIdentifierService();

This is a simple factory pattern based design by which I get to know which system is to be used: DB_BASED or PROPERTY_BASED. You would be using either of these systems, so you can Autowire the respective service directly instead of using a factory pattern based system.

DB_BASED: All experiments and their configurations are stored in mysql server.

PROPERTY_BASED: The system is based on a few simple properties. To change the configuration of an experiment, add/remove an experiment, etc. I’ll have to pass the new properties in command and restart the service.

> List<ExpNameValueModel> expNameValueModelList = expIdentifierGetService.getAllActiveExperiments();

This will give us a list of all experiments. For DB_BASED, I run a simple query to get all experiments. For PROPERTY_BASED, I use the following 3 properties to get the list of all experiments.

> #This property has the names of experiments separated by ';'. Experiment cookies will be set by this name only.
> traffic-split-service.exp-config.keys=exp1;exp2
> #This property has the values of all respective experiments. The values of two different experiments are separated by ';' and values of one particular experiment are separated by ','
> traffic-split-service.exp-config.values=A,B,C,D;true,false
> #This property has the traffic percentage of all respective experiments. The traffic split of two different experiments are separated by ';' and traffic percentages of one particular experiment are separated by ','
> traffic-split-service.exp-config.traffic-split=10,20,30,40;20,80

Based on these properties, I collect the list of of all experiments.

For example, as per above properties, “exp1" will have 4 variations. These variations and their respective traffic divisions are: A:10, B:20, C:30, D:40

> private String getExpValue(long expIdentifierValue, ExpNameValueModel expNameValueModel)

I iterate over each experiment configuration and pass it in this function. I also pass the expIdentifierValue(expId) to this function. This function has the probability distribution logic and returns what value to be given to which experiment based on the value of expIdentifierValue.

Next I simply set the experiment key and value in request attribute, response header and cookie.

# Code Optimizations:
One can add some code optimizations in the repository which I have written as comments(//todo:).

# Why existing open source systems were not suitable for my need❓
I found some open source feature flagging systems but none of them could fulfill all the requirements. This highlights the unique advantages of this system.

I need a flag that can be used by backend and frontend both: There were some systems which were compatible with certain tech stack only. Some other systems had multiple SDKs for different tech stacks but then I had to integrate the system separately in frontend and backend.
I wanted the system to be very fast: I wanted the feature flag to be checked in every API call and the system should work when traffic is very high(scalable). This means that the system should be very fast so that no overhead is added to the API response time. Many open source systems use DBs to store flags for every user/browser session and then the experiment value is queried from the DB. This will make the system slow and also the cost will be high for storing so many sessions.
Many open source systems were free to some extent but after certain traffic limit, they charge money.
Integrating the feature flag should be easy: Integration of feature flagging system should be easy so that it can be integrated in any project of any team easily. Many open source systems have too many features and are too complicated to integrate. Complicated integration means that implementing a minimalistic feature flag could take too much time in the release of a project. To integrate our system, one simply needs to add the PreRequestFilter class in java code and pass some properties.

# 🧐Drawbacks in the system:
Despite being adequate for my needs, the system does have its limitations, which users should be mindful of:

The code is written in java backend only. But I think the same approach can be designed in any backend system and also it can be used by any frontend tech stack as flags are being set as a cookie in the browser.
Sticky session and probability distribution can’t work hand-in-hand: Suppose the traffic split is 50–50% for an experiment. Now if one changes the traffic split to 80–20%, only new sessions will be split into 80–20% since old sessions will be sticky sessions. So one can’t expect the change in traffic split to be instant. It will take time for the sessions to be divided into 80–20% depending on traffic volume. This is an expected behaviour as sticky sessions and changes in probability distribution can’t be done simultaneously. There are some approaches to solve this problem but all of them are heavy in computation(time-taking).


