# Simplified CoNLL2015 Shared Task
The project is the homework of the NLP course (F033569) in SJTU.

* **Director**: Hai Zhao
* **Student**: Kan Ren (014033****)
* **Project Website**: [RK's Github](https://github.com/rk2900/CoNLL2015)

## Task Description
* **Detection**:  Detect the connective phrase in discourse text.
* **Extraction**: Having connective phrase detected, extract the two arguments in the discourse context and output the binary relation of (conn, arg1, arg2).
* **Simplification**: The goal of this project is simplified to detect just *explicit* connective phrases.

## File Organization
* **./lib**: the library JAR files
* **./src**: source file
* **./train**: the train data and the model file
* **./data**: the train data set and the test data set. (including "conll15-st-03-04-15-dev" folder and "conll15-st-03-04-15-dev" folder) The data files are not uploaded onto Github because of size. You may download it from [here](http://nbviewer.ipython.org/github/attapol/conll15st/blob/master/tutorial/tutorial.ipynb).

The entry of the whole project is in Main/main method.
And I use [Maven](http://maven.apache.org/) to manage the library. The configuration file is in *pom.xml*.
If you have no idea about the usage of Maven, you may add all *.jar* files in the folder *./lib*.

The Java environment version is above *JRE 1.8*.

## Solution
My solution is based on [Lin et al.](#lin) and  [Kong et al.](#kong). The task is split into two phases as below.

### Connective Detection
Firstly, we build a connective phrase dictionary for candidate detection. Our dictionary is based on the connective-category list provided by [Knott et al.](#knott). For each sentence, my system detects the appearance of connective phrase found in the dictionary. And then it classify the phrase as whether it is a true connective.

The features used in our setting are listed as below. And *C* is the connective candidate, *prev* is the previous word, and *next* is the next word after *C*. The example sentence is "But its competitors have much broader business  interests and so are better cushioned against price swings.".

Note that my detection component can detect connectives with multiple words, such as "as long as", "in other words" and so on.

| Feature   | Example  | 
| :-------- | :--------:|
| C's POS    |  RB |
| prev + C | and so |
| prev's POS | CC |
| prev's POS + C's POS | CC RB |
| C + next | so are |
| next POS | VBP |
| C's POS + next's POS | RB VBP |

### Argument Extraction
Having detected the connective phrase, the goal is to extract the first argument and the second argument of the connective. My method is based on Kong's paper.

First, my system tries to find the argument candidates in the parse tree. I group the connective phrase in one constituent sub-tree *T*. The candidates are the siblings of *T* and its parent, its grandparent until the root of the parse tree. Also the previous sentence is one of the candidates. 

Secondly, I extract some features of each candidates, then train a model to classify each candidate as "arg1", "arg2" or "null" (means nothing).

Thirdly, I merge the argument candidates with the same predicted labels as the final argument results.

Note that if the connective phrase has multiple words, the nearest parent of the constituent sub-trees of each words in the connective is treated as the sub-tree for the connective.

I implement a parsing algorithm to parse the "parsetree" of each sentence in pdtb-parse.json file.

The features are similar to Kong's work which is listed in Table 1 of the paper.

### Classifier and Configurations
I use Maximum Entropy classifier to train my models. The task of detection is a binary classification task whose labels are "connective" and "non_connective" while extraction is a triple classification task whose labels are "arg1", "arg2" and "null".

The classifier software is maxent in [OpenNLP](https://opennlp.apache.org/index.html).

The smoothing is set off and the iteration number is 100.

## Experiments
I use a splitting ratio of 0.8-to-0.2 to split the data to train the model and evaluate respectively.

The performance of detection is listed as below:
> TP: 2097
> FP: 232
> TN: 5633
> FN: 488
> Precision = 0.9003864319450408
> Recall = 0.8112185686653772
> F1 Score = 0.8534798534798536


We may find that the F1-score is 0.85. The result can be found in ./train/conn_detect_result.txt.

The performance of argument extraction is presented as accuracy since it is a multi-class task
> Accuracy = 0.893971884679533

The final result is output into ./output.json while its format is validated against the official validation method (validator.py).
This output file is generated from the test data set whose label in unknown.


## Reference

[lin]<a name="lin"/>: A PDTB-styled end-to-end discourse parser

[kong]<a name="kong"/>: A Constituent-Based Approach to Argument Labeling with Joint Inference in Discourse Parsing

[knott]<a name="knott"/>: A Data-Driven Methodology for Motivating a Set of Coherence Relations