package netutils

import (
	"crypto/sha256"
	"crypto/tls"
	"crypto/x509"
	"encoding/hex"
	"errors"
	"io/ioutil"
	"net/http"
)

func GetVerify(url string) string {
	shaPin := "8ccd911bf5ac0ed1bce3d4bb227a2aeed7373b5a7259ec162251fb945c9fad57"
	config := &tls.Config{
		InsecureSkipVerify: true,
	}

	config.VerifyPeerCertificate = func(certificates [][]byte, _ [][]*x509.Certificate) error {
		certs := make([]*x509.Certificate, len(certificates))
		for i, asn1Data := range certificates {
			cert, err := x509.ParseCertificate(asn1Data)
			if err != nil {
				return errors.New("tls: failed to parse certificate from server: " + err.Error())
			}
			certs[i] = cert
		}
		cepk, err := x509.MarshalPKIXPublicKey(certs[0].PublicKey)
		if err != nil {
			return err
		}
		pkh := sha256.New()
		pkh.Write(cepk)
		pubKeyHash := hex.EncodeToString(pkh.Sum(nil))

		if pubKeyHash != shaPin {
			return errors.New("cannot verify certificate")
		}
		return nil
	}
	client := &http.Client{Transport: &http.Transport{TLSClientConfig: config}}
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return err.Error()
	}
	resp, err := client.Do(req)
	if err != nil {
		return err.Error()
	}
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return err.Error()
	}
	return string(body)
}
