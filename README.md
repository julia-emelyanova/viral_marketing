
#### Overview 

This project investigates the information flow and influence diffusion within a social network. 

#### Questions  
1. How many generations would it require to reach equilibrium when the given set of users adopt new behavior?
2. How we should select a fixed number of initial users, that would trigger the largest number of users to adopt desired behavior?  
3. How I can visualize both solutions?

#### Algorithms, Data Structures, and Answer to your Question  

The main data structure is a graph, where vertex represents a user, and an edge is a friendship.  Graph represented with an adjacency list -  HashMap<Integer, HashSet<Integer>>. 

##### Question 1

In the beginning, we have several user ids, who have adopted the new behavior, let's call them nodesUsingA. We'll use some data structure  nodesSwitchedInCurrentGeneration to track users, who switched in the current generation     

1. init  nodesUsingA, nodesSwitchedInCurrentGeneration  
2. repeat until nodesSwitchedInCurrentGeneration  is empty  
3.     for each user U in nodesUsingA
4.         for each friend F of user U 
5.           if user F would adopt new behavior add it to nodesSwitchedInCurrentGeneration                    
6.     merge  nodesUsingA with nodesSwitchedInCurrentGeneration 
  

##### Question 2

A straightforward solution - just repeat an algorithm from the easier question for all combinations of users for number K. That would take too much time (big O notation below) , so we'll need to find an approximate solution and my initial idea is inspired by gradient descent:

1. Select random K users 
2. Run an initial algorithm for these users and save the result
3. Take one user U from the initial set, replace it with its friend, run an initial algorithm and see if there is any positive change. Here we use LInkedLIst<Integer> to make sure our users are ordered. Repeat for every friend of U
4. Repeat for every user from the initial set or until we don't feel like running anymore

#####  Question 3 (visualization)

I used the GraphStream library to make visualizations. 






#### Algorithm analysis

Q1Calculating if a user would switch (line 5) would require O(|V|) time - we'd have to take all of the friends of a user, see how many of them have adopted a new behavior. Then we'd have to do that for all of the friends of the user U (line 3) (complexity O(|V|)), which makes our algorithm quadratic. Merging two data structures (line 6) would take O(|V|)  time, which is less than O(|V|^2), so we can ignore it. If we'd run it until we reach equilibrium (line 2), it'd take O(|V|^3) time  

Q2The number of operations is a combination without repetition C v,k multiplied by the complexity of initial algorithm =  (V!/K!(V-K)!) * O(V^3). That is the brute force algorithm for our harder question would run in O(V^(k+3)) time. Research on the Internet (https://link.springer.com/article/10.1007/s41109-018-0062-7) shows, that a harder question is an Influence maximization problem for the linear threshold diffusion model, which is proved to be an NP-hard problem. The approximate solution is still O(|V|^3) 


#### Correctness verification (i.e. testing)

I've debugged the program for a small set. Then I've run a program for different initial settings - rewards, a number of initially switched nodes. Then I've extended the dataset and repeat testing and reviewed and compared results - do they make sense? Do they seem true? For example, if I increase a reward for switching behavior, the number of switched users, in the end, should not decrease. And then I decided to visualize it to see that I've implemented everything correctly - and it helped a lot.
