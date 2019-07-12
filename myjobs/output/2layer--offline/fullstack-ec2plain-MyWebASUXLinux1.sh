aws cloudformation create-stack --stack-name org-ASUX-Playground-Tokyo-2layer-EC2-MyWebASUXLinux1  --region ap-northeast-1 --profile ${AWSprofile} --parameters  ParameterKey=MyPublicSubnet1,ParameterValue=org-ASUX-Playground-Tokyo-2layer-Subnet-Public1-ID ParameterKey=MySSHSecurityGroup,ParameterValue=org-ASUX-Playground-Tokyo-2layer-SG-SSH ParameterKey=MyIamInstanceProfiles,ParameterValue=EC2-ReadWrite-to-S3 ParameterKey=AWSAMIID,ParameterValue=ami-084040f99a74ce8c3  ParameterKey=EC2InstanceType,ParameterValue=t2.micro  ParameterKey=MySSHKeyName,ParameterValue=Tokyo-org-ASUX-Playground-LinuxSSH.pem --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layer/fullstack-ec2plain-MyWebASUXLinux1.yaml