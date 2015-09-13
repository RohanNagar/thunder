import sys
import boto
import troposphere.autoscaling as autoscale
import troposphere.ec2 as ec2
import troposphere.elasticloadbalancing as elb
import troposphere.iam as iam
from troposphere import Ref, Tags, Template


def main():
    vpc = get_vpc()
    if vpc is None:
        raise LookupError("Unable to connect to VPC.")

    subnets = get_all_subnets(vpc)

    template = get_template(vpc, subnets)

    cfn = boto.connect_cloudformation()

    print("Creating CloudFormation stack in AWS.")
    cfn.create_stack(
        'thunder',
        template_body=template,
        template_url=None,
        parameters=[],
        notification_arns=[],
        disable_rollback=False,
        timeout_in_minutes=None,
        capabilities=["CAPABILITY_IAM"],
        tags=dict(
            application="thunder",
            environment="test"
        )
    )
    print("Stack creation complete.")


def get_vpc():
    vpc_conn = boto.connect_vpc()
    vpc_list = vpc_conn.get_all_vpcs(vpc_ids=["vpc-90ba7bf4"])

    return vpc_list[0] if vpc_list else None


def get_all_subnets(vpc):
    vpc_conn = boto.connect_vpc()

    return vpc_conn.get_all_subnets(filters={'vpc_id': vpc.id})

def get_template(vpc, subnets):
    template = Template()
    template.add_description("Creates Thunder.")

    # Security group for ELB
    sg = template.add_resource(ec2.SecurityGroup(
        "ELBSecurityGroup",
        GroupDescription="Security Group for thunder in test",
        VpcId=vpc.id,
        SecurityGroupIngress=[
            ec2.SecurityGroupRule(
                IpProtocol="tcp",
                FromPort="80",
                ToPort="80",
                CidrIp="0.0.0.0/0"
            ),
            ec2.SecurityGroupRule(
                IpProtocol="tcp",
                FromPort="8081",
                ToPort="8081",
                CidrIp="0.0.0.0/0"
            )
        ],
        Tags=Tags(
            Name="thunder-test-elb",
            application="thunder",
            environment="test"
        )
    ))

    # ELB
    lb = template.add_resource(elb.LoadBalancer(
        "LoadBalancer",
        SecurityGroups=[Ref(sg)],
        Subnets=[subnet.id for subnet in subnets],
        Scheme="internet-facing",
        Listeners=[
            elb.Listener(
                LoadBalancerPort="80",
                InstancePort="8080",
                Protocol="HTTP"
            ),
            elb.Listener(
                LoadBalancerPort="8081",
                InstancePort="8081",
                Protocol="HTTP"
            )
        ],
        HealthCheck=elb.HealthCheck(
            Target="HTTP:80/",
            HealthyThreshold="3",
            UnhealthyThreshold="3",
            Interval="30",
            Timeout="5"
        ),
        Tags=Tags(
            Name="thunder-test-elb",
            application="thunder",
            environment="test"
        )
    ))

    # Security group for EC2
    sg = template.add_resource(ec2.SecurityGroup(
        "SecurityGroup",
        GroupDescription="Security group for thunder instances in test",
        VpcId=vpc.id,
        SecurityGroupIngress=[
            ec2.SecurityGroupRule(
                IpProtocol="tcp",
                FromPort="22",
                ToPort="22",
                CidrIp="0.0.0.0/0"
            ),
            ec2.SecurityGroupRule(
                IpProtocol="tcp",
                FromPort="8080",
                ToPort="8081",
                CidrIp="0.0.0.0/0"
            )
        ],
        Tags=Tags(
            Name="thunder-test-elb",
            application="thunder",
            environment="test"
        )
    ))

    # Instance role (Allow S3 and Dynamo reads)
    role = template.add_resource(iam.Role(
        "InstanceRole",
        AssumeRolePolicyDocument={
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": {
                        "Service": ["ec2.amazonaws.com"]
                    },
                    "Action": ["sts:AssumeRole"]
                }
            ]
        },
        Path="/",
        Policies=[
            iam.Policy(
                PolicyName="ReadFromS3AndDynamo",
                PolicyDocument={
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Resource": "arn:aws:s3:::artifacts.sanction.com",
                            "Action": [
                                "s3:ListBucket"
                            ]
                        },
                        {
                            "Effect": "Allow",
                            "Resource": [
                                "arn:aws:s3:::artifacts.sanction.com/maven/releases/*"
                            ],
                            "Action": [
                                "s3:GetObject"
                            ]
                        },
                        {
                            "Effect": "Allow",
                            "Action": "dynamodb:*",
                            "Resource": "*"
                        },
                        {
                            "Effect": "Deny",
                            "Action": [
                                "dynamodb:DeleteTable",
                                "dynamodb:CreateTable"
                            ],
                            "Resource":"*"
                        }
                    ]
                }
            )
        ]
    ))

    # Set role to instance profile
    profile = template.add_resource(iam.InstanceProfile(
        "InstanceProfile",
        Path="/",
        Roles=[Ref(role)]
    ))

    # Launch configuration
    launch_config = template.add_resource(autoscale.LaunchConfiguration(
        "LaunchConfig",
        ImageId="ami-0d4cfd66",
        InstanceType="t2.micro",
        InstanceMonitoring=False,
        KeyName="engineering",
        SecurityGroups=[Ref(sg)],
        IamInstanceProfile=Ref(profile)
    ))

    # ASG
    template.add_resource(autoscale.AutoScalingGroup(
        "AutoScalingGroup",
        AvailabilityZones=[subnet.availability_zone for subnet in subnets],
        LaunchConfigurationName=Ref(launch_config),
        MinSize=1,
        MaxSize=1,
        VPCZoneIdentifier=[subnet.id for subnet in subnets],
        HealthCheckType="EC2",
        LoadBalancerNames=[Ref(lb)],
        Tags=autoscale.Tags(
            Name="thunder-test-elb",
            application="thunder",
            environment="test"
        )
    ))

    return template.to_json()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(1)
    except IOError:
        sys.exit(1)

    sys.exit(0)
