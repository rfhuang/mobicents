#Don't forget to kill the sipp uas process after the test run it might still hang
../sipp 127.0.0.1:5080 -sf call-forwarding-receiver.xml -trace_err -i 127.0.0.1 -p 5090 -bg -timeout 20
../sipp 127.0.0.1:5080 -s receiver -sf call-forwarding-sender.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 2
sleep 10
