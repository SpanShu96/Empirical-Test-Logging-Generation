# Exploring the Effectiveness of Generating Log Statements in Test Codes

In this study, we empirically investigate various logging-specific PLMs and general-/code-specific LLMs with different learning strategies (e.g., in-context leraning, instruction tuning) for generating test log statements. This is a replication package for our empirical study.

#### Dataset
The dataset for pre-training, fine-tuning/instruction-tuning (train, val, and test set), and in-context learning can be found as this link:  https://drive.google.com/drive/folders/1FZDgEmfvIMGZ8llL43fv1BoqreYc_PNV?usp=sharing

#### How to experiment with logging-specific PLMs
In this experiment, we mainly focus only the automatic logging generation technique, [LANCE](https://github.com/antonio-mastropaolo/LANCE?tab=readme-ov-file), a T5-based approach for supporting the task of log statement generation and injection. In this case, we mainly use Google Colab to implament original LANCE and LANCE-T (a variant LANCE version for test logging generation). Please provide your own google account with Colab membership for excuting the code and also store the dataset in Google Cloud Storage.

* Pre-trained Tokenizer for LANCE
  Tokenizer is important for accomodating the expanded vocabulary given by the Java programming language. To save the afford, we used the original tokenizer provied by LANCE: 
* Setup a Google Cloud Storage (GCS) Bucket
  
* Inference for LANCE
* Pre-training/Fine-tuning for LANCE-T
* Additional
* 

#### How to experiment with general-/code-specific LLMs

