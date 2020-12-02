<?php

$json = json_decode(file_get_contents('php://input'), true);
$profile = $_GET['profile'];

if($json['type'] === 'connection_discovery') {
    echo '{"type":"connection_discovery","ver":"0.3","acceptedTokens":[{"method":"spxp.org:webflow:1.0","start":"http://testbed.spxp.org/0.3/_acquire-token.php?profile='.$profile.'"}]}';
    http_response_code(200);
} else if($json['type'] === 'connection_request') {
    http_response_code(204);
} else {
    http_response_code(500);
}

?>